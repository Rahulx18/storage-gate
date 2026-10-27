package com.personal.storagegate.service;

import com.personal.storagegate.entity.AppUser;
import com.personal.storagegate.entity.Permission;
import com.personal.storagegate.entity.SignupRequest;
import com.personal.storagegate.repository.AppUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Service
public class MailService {

    private static final String APPROVE_COLOR = "#16a34a";
    private static final String REJECT_COLOR = "#dc2626";

    private final JavaMailSender mailSender;
    private final AppUserRepository userRepository;
    private final String template;

    @Value("${mail.from}")
    private String from;

    public MailService(JavaMailSender mailSender,
                       AppUserRepository userRepository,
                       @Value("classpath:templates/mail/layout.html") Resource templateResource) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.template = readTemplate(templateResource);
    }

    public void notifyAdmins(SignupRequest request) {
        String[] admins = userRepository.findAllByPermission(Permission.ADMIN).stream()
                .map(AppUser::getUsername)
                .toArray(String[]::new);
        if (admins.length == 0) {
            log.warn("No admin users to notify about signup request {}", request.getId());
            return;
        }

        String email = request.getEmail();
        String ip = request.getRequestedIp();
        String approveUrl = "/admin/signup-requests/" + request.getId() + "/approve";
        String rejectUrl = "/admin/signup-requests/" + request.getId() + "/reject";

        String plainText = "A new signup request was submitted.\n\n"
                + email + " requested access from IP " + ip + ".\n\n"
                + "Approve: POST " + approveUrl + "\n"
                + "Reject:  POST " + rejectUrl;

        String html = render(
                "New signup request",
                "A new signup request was submitted."
                        + "<br/><br/><strong>" + escape(email) + "</strong>"
                        + " requested access from IP <strong>" + escape(ip) + "</strong>.",
                button(approveUrl, "Approve", APPROVE_COLOR)
                        + button(rejectUrl, "Reject", REJECT_COLOR));

        send(admins, "Signup request: " + email, plainText, html);
    }

    public void sendApproved(String email) {
        String plainText = "Your signup request was approved. You can now log in with your email and password.";
        String html = render(
                "Access approved",
                "Your signup request was approved. You can now log in with your email and password.",
                "");
        send(new String[]{email}, "Your access request was approved", plainText, html);
    }

    private String render(String title, String bodyHtml, String actionHtml) {
        return template
                .replace("{{title}}", title)
                .replace("{{body}}", bodyHtml)
                .replace("{{action}}", actionHtml == null ? "" : actionHtml);
    }

    private static String button(String href, String label, String color) {
        return "<a href=\"" + href + "\" style=\"display:inline-block; background-color:" + color
                + "; color:#ffffff; text-decoration:none; padding:12px 24px; border-radius:6px;"
                + " font-size:14px; font-weight:600; margin:4px 12px 4px 0;\">" + label + "</a>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String readTemplate(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read mail template from " + resource, e);
        }
    }

    private void send(String[] to, String subject, String plainText, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send mail to {}", Arrays.toString(to), e);
        }
    }
}
