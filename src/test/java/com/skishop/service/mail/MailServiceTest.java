package com.skishop.service.mail;

import com.skishop.dao.mail.EmailQueueDao;
import com.skishop.domain.mail.EmailQueue;
import com.skishop.domain.order.Order;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock private EmailQueueDao emailQueueDao;
    @Mock private JavaMailSender mailSender;
    @Mock private MimeMessage mimeMessage;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(emailQueueDao, mailSender);
        ReflectionTestUtils.setField(mailService, "mailFrom", "test@example.com");
    }

    @Test
    void enqueuePasswordReset_createsEmailQueueRecord() {
        mailService.enqueuePasswordReset("user@example.com", "reset-token-123");

        ArgumentCaptor<EmailQueue> captor = ArgumentCaptor.forClass(EmailQueue.class);
        verify(emailQueueDao).enqueue(captor.capture());
        EmailQueue mail = captor.getValue();
        assertEquals("user@example.com", mail.getToAddr());
        assertEquals("Password reset", mail.getSubject());
        assertTrue(mail.getBody().contains("reset-token-123"));
        assertEquals("PENDING", mail.getStatus());
    }

    @Test
    void enqueueOrderConfirmation_createsEmailQueueRecord() {
        Order order = new Order();
        order.setOrderNumber("ORD-123");
        order.setTotalAmount(BigDecimal.valueOf(15000));

        mailService.enqueueOrderConfirmation("user@example.com", order);

        ArgumentCaptor<EmailQueue> captor = ArgumentCaptor.forClass(EmailQueue.class);
        verify(emailQueueDao).enqueue(captor.capture());
        EmailQueue mail = captor.getValue();
        assertEquals("user@example.com", mail.getToAddr());
        assertEquals("Order confirmation", mail.getSubject());
        assertTrue(mail.getBody().contains("ORD-123"));
        assertTrue(mail.getBody().contains("15000"));
        assertEquals("PENDING", mail.getStatus());
    }

    @Test
    void processQueue_sendsPendingEmails_updatesStatus() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        EmailQueue mail = new EmailQueue();
        mail.setId("m-1");
        mail.setToAddr("user@example.com");
        mail.setSubject("Test");
        mail.setBody("body");
        mail.setStatus("PENDING");
        mail.setRetryCount(0);
        mail.setScheduledAt(new Date(System.currentTimeMillis() - 10000));
        when(emailQueueDao.findByStatus("PENDING")).thenReturn(List.of(mail));

        mailService.processQueue();

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailQueueDao).updateStatus(eq("m-1"), eq("SENT"), eq(0), isNull(), any(), any());
    }

    @Test
    void processQueue_marksFailedAfterMaxRetries() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Use null toAddr to trigger MessagingException from sanitizeAddress
        EmailQueue mail = new EmailQueue();
        mail.setId("m-1");
        mail.setToAddr(null);
        mail.setSubject("Test");
        mail.setBody("body");
        mail.setStatus("PENDING");
        mail.setRetryCount(2);
        mail.setScheduledAt(new Date(System.currentTimeMillis() - 10000));
        when(emailQueueDao.findByStatus("PENDING")).thenReturn(List.of(mail));

        mailService.processQueue();

        verify(emailQueueDao).updateStatus(eq("m-1"), eq("FAILED"), eq(3), anyString(), isNull(), isNull());
    }
}
