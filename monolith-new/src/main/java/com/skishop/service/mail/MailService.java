package com.skishop.service.mail;

import com.skishop.dao.mail.EmailQueueDao;
import com.skishop.domain.mail.EmailQueue;
import com.skishop.domain.order.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

@Service
public class MailService {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 60000L;

    private static final String PASSWORD_RESET_TEMPLATE = loadTemplate(
            "mail/password_reset.txt",
            "Password reset token: {{token}}\n");
    private static final String ORDER_CONFIRMATION_TEMPLATE = loadTemplate(
            "mail/order_confirmation.txt",
            "Thank you for your order.\nOrder: {{orderNumber}}\nTotal: {{totalAmount}}\n");

    private final EmailQueueDao emailQueueDao;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public MailService(EmailQueueDao emailQueueDao,
                       JavaMailSender mailSender,
                       @Value("${spring.mail.from:no-reply@skishop.local}") String mailFrom) {
        this.emailQueueDao = emailQueueDao;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
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
        String body = replace(PASSWORD_RESET_TEMPLATE, "{{token}}", token);
        enqueue(to, "Password reset", body);
    }

    public void enqueueOrderConfirmation(String to, Order order) {
        String body = ORDER_CONFIRMATION_TEMPLATE;
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
        helper.setFrom(mailFrom);
        helper.setTo(mail.getToAddr());
        helper.setSubject(mail.getSubject());
        helper.setText(mail.getBody());
        helper.setSentDate(new Date());
        mailSender.send(message);
    }

    private static String loadTemplate(String path, String fallback) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return fallback;
            }
            try (InputStream input = resource.getInputStream()) {
                return readText(input);
            }
        } catch (IOException e) {
            return fallback;
        }
    }

    private static String readText(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    private static String replace(String template, String token, String value) {
        if (template == null) {
            return value;
        }
        if (value == null) {
            value = "";
        }
        return template.replace(token, value);
    }

    private static String formatError(Exception e) {
        String message = e.getMessage();
        String detail = e.getClass().getName();
        if (message != null && !message.isEmpty()) {
            detail = detail + ": " + message;
        }
        if (detail.length() > 500) {
            detail = detail.substring(0, 500);
        }
        return detail;
    }
}
