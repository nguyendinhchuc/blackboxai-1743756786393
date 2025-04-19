package com.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private final RevisionNotificationConfig revisionNotificationConfig;

    public MailConfig(RevisionNotificationConfig revisionNotificationConfig) {
        this.revisionNotificationConfig = revisionNotificationConfig;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        RevisionNotificationConfig.EmailConfig emailConfig = revisionNotificationConfig.getEmail();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());

        mailSender.setUsername(emailConfig.getUsername());
        mailSender.setPassword(emailConfig.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailConfig.getProtocol());
        props.put("mail.smtp.auth", String.valueOf(emailConfig.getAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailConfig.getStartTlsEnabled()));
        props.put("mail.smtp.starttls.required", String.valueOf(emailConfig.getStartTlsRequired()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(emailConfig.getConnectionTimeout()));
        props.put("mail.smtp.timeout", String.valueOf(emailConfig.getTimeout()));
        props.put("mail.smtp.writetimeout", String.valueOf(emailConfig.getWriteTimeout()));

        return mailSender;
    }
}
