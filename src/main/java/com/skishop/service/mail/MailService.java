package com.skishop.service.mail;

import com.skishop.dao.mail.EmailQueueDao;
import com.skishop.domain.mail.EmailQueue;
import com.skishop.domain.order.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 60000L;
    private static final String TEMPLATE_PASSWORD_RESET = "mail/password_reset.txt";
    private static final String TEMPLATE_ORDER_CONFIRMATION = "mail/order_confirmation.txt";

    private final String passwordResetTemplate;
    private final String orderConfirmationTemplate;

    private final EmailQueueDao emailQueueDao;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.from:no-reply@localhost}")
    private String mailFrom;

    public MailService(EmailQueueDao emailQueueDao, JavaMailSender mailSender) {
        this.emailQueueDao = emailQueueDao;
        this.mailSender = mailSender;
        this.passwordResetTemplate = loadTemplate(TEMPLATE_PASSWORD_RESET,
                "Password reset token: {{token}}\n");
        this.orderConfirmationTemplate = loadTemplate(TEMPLATE_ORDER_CONFIRMATION,
                "Thank you for your order.\nOrder: {{orderNumber}}\nTotal: {{totalAmount}}\n");
    }

    public void enqueue(String to, String subject, String body) {
        EmailQueue mail = new EmailQueue();
        mail.setId(UUID.randomUUID().toString());
        mail.setToAddr(to);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setStatus("PENDING");
        mail.setRetryCount(0);
        mail.setLastError(null);
        mail.setScheduledAt(new Date());
        mail.setSentAt(null);
        emailQueueDao.enqueue(mail);
    }

    public void enqueuePasswordReset(String to, String token) {
        String body = replace(passwordResetTemplate, "{{token}}", token);
        enqueue(to, "Password reset", body);
    }

    public void enqueueOrderConfirmation(String to, Order order) {
        String body = orderConfirmationTemplate;
        body = replace(body, "{{orderNumber}}", order != null ? order.getOrderNumber() : "");
        body = replace(body, "{{totalAmount}}",
                order != null && order.getTotalAmount() != null ? order.getTotalAmount().toString() : "");
        enqueue(to, "Order confirmation", body);
    }

    @Scheduled(fixedDelay = 30000)
    public void processQueue() {
        List<EmailQueue> pending = emailQueueDao.findByStatus("PENDING");
        Date now = new Date();
        for (EmailQueue mail : pending) {
            if (mail.getScheduledAt() != null && mail.getScheduledAt().after(now)) {
                continue;
            }
            handleSend(mail, now);
        }
    }

    private void handleSend(EmailQueue mail, Date now) {
        try {
            send(mail);
            emailQueueDao.updateStatus(mail.getId(), "SENT", mail.getRetryCount(), null,
                    mail.getScheduledAt(), new Date());
        } catch (MessagingException e) {
            int retryCount = mail.getRetryCount() + 1;
            String status = retryCount >= MAX_RETRY ? "FAILED" : "PENDING";
            Date nextSchedule = null;
            if ("PENDING".equals(status)) {
                nextSchedule = new Date(now.getTime() + RETRY_DELAY_MS);
            }
            emailQueueDao.updateStatus(mail.getId(), status, retryCount, formatError(e),
                    nextSchedule, null);
        }
    }

    private void send(EmailQueue mail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(sanitizeAddress(mailFrom));
        helper.setTo(sanitizeAddress(mail.getToAddr()));
        helper.setSubject(mail.getSubject());
        helper.setSentDate(new Date());
        helper.setText(mail.getBody());
        mailSender.send(message);
    }

    private static String loadTemplate(String path, String fallback) {
        InputStream input = MailService.class.getClassLoader().getResourceAsStream(path);
        if (input == null) return fallback;
        try {
            return readText(input);
        } catch (IOException e) {
            return fallback;
        }
    }

    private static String readText(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    private static String replace(String template, String token, String value) {
        if (template == null) return value;
        return template.replace(token, value != null ? value : "");
    }

    private static String formatError(Exception e) {
        String detail = e.getClass().getName();
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) detail = detail + ": " + message;
        if (detail.length() > 500) detail = detail.substring(0, 500);
        return detail;
    }

    private String sanitizeAddress(String address) throws MessagingException {
        if (address == null) throw new MessagingException("Email address is required");
        String trimmed = address.trim();
        if (trimmed.indexOf('\n') >= 0 || trimmed.indexOf('\r') >= 0)
            throw new MessagingException("Invalid email address");
        return trimmed;
    }
}
