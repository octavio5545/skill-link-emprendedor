package com.example.demo.user.service;

import com.example.demo.user.template.EmailTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplateBuilder templateBuilder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    public CompletableFuture<Void> enviarCorreoRecuperacion(String destinatario, String nombreUsuario, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "SkillLink - Plataforma Emprendedora");
            helper.setTo(destinatario);
            helper.setSubject("🔐 Recuperación de Contraseña - SkillLink");

            String enlaceRecuperacion = frontendUrl + "/reset-password?token=" + token;
            String contenidoHtml = templateBuilder.construirPlantillaRecuperacion(nombreUsuario, enlaceRecuperacion);

            helper.setText(contenidoHtml, true);
            mailSender.send(message);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }
}