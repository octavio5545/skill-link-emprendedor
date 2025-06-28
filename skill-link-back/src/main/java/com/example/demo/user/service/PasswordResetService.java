package com.example.demo.user.service;

import com.example.demo.user.dto.ForgotPasswordRequest;
import com.example.demo.user.dto.PasswordResetResponse;
import com.example.demo.user.dto.ResetPasswordRequest;
import com.example.demo.user.model.PasswordResetToken;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.PasswordResetTokenRepository;
import com.example.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.password-reset.expiration-minutes:15}")
    private int expirationMinutes;

    @Transactional
    public PasswordResetResponse solicitarRecuperacion(ForgotPasswordRequest request) {
        try {
            User usuario = userRepository.findUserByEmail(request.correo())
                    .orElse(null);

            String mensajeSeguro = "Si el correo electrónico está registrado, recibirás un enlace de recuperación en breve.";
            if (usuario == null) {
                System.out.println("Correo no registrado: " + request.correo());
                return PasswordResetResponse.exito(mensajeSeguro, request.correo());
            }

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            int tokensExpiradosEliminados = passwordResetTokenRepository.deleteExpiredTokens(now);

            if (passwordResetTokenRepository.existsValidTokenForUsuario(usuario, now)) {
                return PasswordResetResponse.exito(mensajeSeguro, request.correo());
            }
            passwordResetTokenRepository.deleteByUsuario(usuario);

            String tokenValue = UUID.randomUUID().toString();
            OffsetDateTime expiry = now.plusMinutes(expirationMinutes);

            PasswordResetToken token = new PasswordResetToken(tokenValue, usuario, now, expiry);
            passwordResetTokenRepository.save(token);

            String nombreUsuario = usuario.getName() + " " + usuario.getSecondName();
            emailService.enviarCorreoRecuperacion(request.correo(), nombreUsuario, tokenValue)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            System.err.println("Error al enviar correo: " + throwable.getMessage());
                        } else {
                            System.out.println("Correo procesado correctamente");
                        }
                    });
            return PasswordResetResponse.exito(mensajeSeguro, request.correo());
        } catch (Exception e) {
            System.err.println("Error al procesar solicitud de recuperación para: " + request.correo());
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor. Inténtalo más tarde.");
        }
    }

    public PasswordResetResponse validarToken(String token) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                    .orElse(null);
            if (resetToken == null) {
                return PasswordResetResponse.error("Enlace inválido o expirado.");
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (!resetToken.isValido()) {
                if (resetToken.isUsado()) {
                    return PasswordResetResponse.error("Este enlace ya fue utilizado.");
                } else if (resetToken.isExpirado()) {
                    return PasswordResetResponse.error("Este enlace ha expirado. Solicita uno nuevo.");
                } else {
                    return PasswordResetResponse.error("Enlace inválido.");
                }
            }
            return PasswordResetResponse.exito("Token válido", resetToken.getUsuario().getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor.");
        }
    }


    @Transactional
    public PasswordResetResponse cambiarContrasena(ResetPasswordRequest request) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                    .orElse(null);
            if (resetToken == null) {
                return PasswordResetResponse.error("Enlace inválido o expirado.");
            }
            if (!resetToken.isValido()) {
                if (resetToken.isUsado()) {
                    return PasswordResetResponse.error("Este enlace ya fue utilizado.");
                } else if (resetToken.isExpirado()) {
                    return PasswordResetResponse.error("Este enlace ha expirado. Solicita uno nuevo.");
                } else {
                    return PasswordResetResponse.error("Enlace inválido.");
                }
            }

            User usuario = resetToken.getUsuario();
            String nuevaContraEncriptada = passwordEncoder.encode(request.nuevaContra());
            usuario.setPassword(nuevaContraEncriptada);

            userRepository.save(usuario);
            resetToken.marcarComoUsado();
            passwordResetTokenRepository.save(resetToken);
            return PasswordResetResponse.exito(
                    "Contraseña cambiada exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.",
                    usuario.getEmail()
            );

        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña con token: " + request.token());
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor. Inténtalo más tarde.");
        }
    }

    @Transactional
    public void limpiarTokensExpirados() {
        try {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            int tokensEliminados = passwordResetTokenRepository.deleteExpiredTokens(now);
        } catch (Exception e) {
            System.err.println("Error al limpiar tokens expirados");
            e.printStackTrace();
        }
    }
}