package com.eventeanagementsystem.event_management_system.config;

import com.eventeanagementsystem.event_management_system.util.EnvUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Java-based mail configuration that reads credentials from environment variables
 * (or a local .env via EnvUtils) and does not rely on Spring Boot's spring.mail.* properties.
 */
@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        String host = EnvUtils.firstNonEmpty(
                System.getenv("MAIL_HOST"),
                EnvUtils.getFromDotenv("MAIL_HOST"),
                "sandbox.smtp.mailtrap.io"
        );
        int port = EnvUtils.firstNonEmptyInt(
                EnvUtils.getFromSystemOrDotenv("MAIL_PORT"),
                2525
        );
        String username = EnvUtils.firstNonEmpty(
                System.getenv("MAIL_USERNAME"),
                EnvUtils.getFromDotenv("MAIL_USERNAME")
        );
        String password = EnvUtils.firstNonEmpty(
                System.getenv("MAIL_PASSWORD"),
                EnvUtils.getFromDotenv("MAIL_PASSWORD")
        );

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
