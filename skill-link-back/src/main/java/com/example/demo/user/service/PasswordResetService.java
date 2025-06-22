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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Configuración del tiempo de expiración (en minutos)
    @Value("${app.password-reset.expiration-minutes:15}")
    private int expirationMinutes;

    /**
     * Genera y envía un token de recuperación de contraseña
     */
    @Transactional
    public PasswordResetResponse solicitarRecuperacion(ForgotPasswordRequest request) {
        try {
            System.out.println("🔍 Iniciando proceso de recuperación para: " + request.correo());

            User usuario = userRepository.findUserByEmail(request.correo())
                    .orElse(null);

            String mensajeSeguro = "Si el correo electrónico está registrado, recibirás un enlace de recuperación en breve.";

            if (usuario == null) {
                System.out.println("⚠️ Correo no registrado: " + request.correo());
                return PasswordResetResponse.exito(mensajeSeguro, request.correo());
            }

            // Limpiar tokens expirados ANTES de verificar
            LocalDateTime now = LocalDateTime.now();
            int tokensExpiradosEliminados = passwordResetTokenRepository.deleteExpiredTokens(now);
            System.out.println("🧹 Tokens expirados eliminados: " + tokensExpiradosEliminados);

            //  Verificar si ya existe un token válido DESPUÉS de limpiar
            if (passwordResetTokenRepository.existsValidTokenForUsuario(usuario, now)) {
                System.out.println("⏰ Ya existe un token válido para: " + request.correo());
                return PasswordResetResponse.exito(mensajeSeguro, request.correo());
            }

            //  Eliminar TODOS los tokens anteriores del usuario
            passwordResetTokenRepository.deleteByUsuario(usuario);
            System.out.println("🗑️ Tokens anteriores eliminados para: " + request.correo());

            // 5. Crear nuevo token
            String tokenValue = UUID.randomUUID().toString();
            LocalDateTime expiry = now.plusMinutes(expirationMinutes);

            PasswordResetToken token = new PasswordResetToken(tokenValue, usuario, now, expiry);
            passwordResetTokenRepository.save(token);

            // 6. Log del token generado
            System.out.println("✅ Token de recuperación generado para " + request.correo() + ": " + tokenValue);
            System.out.println("📧 Enlace de recuperación: http://localhost:5173/reset-password?token=" + tokenValue);
            System.out.println("⏰ Token expira el: " + expiry);

            return PasswordResetResponse.exito(mensajeSeguro, request.correo());

        } catch (Exception e) {
            System.err.println("❌ Error al procesar solicitud de recuperación para: " + request.correo());
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor. Inténtalo más tarde.");
        }
    }

    /**
     * Valida un token de recuperación
     */
    public PasswordResetResponse validarToken(String token) {
        try {
            System.out.println("🔍 Validando token: " + token);

            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                    .orElse(null);

            if (resetToken == null) {
                System.out.println("❌ Token no encontrado: " + token);
                return PasswordResetResponse.error("Enlace inválido o expirado.");
            }

            // Log detallado para debugging
            System.out.println("📋 Token encontrado:");
            System.out.println("   - Usado: " + resetToken.isUsado());
            System.out.println("   - Expira: " + resetToken.getFechaExpiracion());
            System.out.println("   - Ahora: " + LocalDateTime.now());
            System.out.println("   - Es válido: " + resetToken.isValido());

            if (!resetToken.isValido()) {
                if (resetToken.isUsado()) {
                    System.out.println("⚠️ Token ya fue usado: " + token);
                    return PasswordResetResponse.error("Este enlace ya fue utilizado.");
                } else if (resetToken.isExpirado()) {
                    System.out.println("⏰ Token expirado: " + token + " - Expiró: " + resetToken.getFechaExpiracion());
                    return PasswordResetResponse.error("Este enlace ha expirado. Solicita uno nuevo.");
                } else {
                    System.out.println("❓ Token inválido por razón desconocida: " + token);
                    return PasswordResetResponse.error("Enlace inválido.");
                }
            }

            System.out.println("✅ Token válido para usuario: " + resetToken.getUsuario().getEmail());
            return PasswordResetResponse.exito("Token válido", resetToken.getUsuario().getEmail());

        } catch (Exception e) {
            System.err.println("❌ Error al validar token: " + token);
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor.");
        }
    }

    /**
     * Cambia la contraseña usando el token de recuperación
     */
    @Transactional
    public PasswordResetResponse cambiarContrasena(ResetPasswordRequest request) {
        try {
            System.out.println("🔄 Iniciando cambio de contraseña con token: " + request.token());

            // 1. Buscar y validar token
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                    .orElse(null);

            if (resetToken == null) {
                System.out.println("❌ Token no encontrado para cambio de contraseña: " + request.token());
                return PasswordResetResponse.error("Enlace inválido o expirado.");
            }

            // Validación más específica
            if (!resetToken.isValido()) {
                if (resetToken.isUsado()) {
                    System.out.println("⚠️ Intento de usar token ya utilizado: " + request.token());
                    return PasswordResetResponse.error("Este enlace ya fue utilizado.");
                } else if (resetToken.isExpirado()) {
                    System.out.println("⏰ Intento de usar token expirado: " + request.token() + " - Expiró: " + resetToken.getFechaExpiracion());
                    return PasswordResetResponse.error("Este enlace ha expirado. Solicita uno nuevo.");
                } else {
                    System.out.println("❓ Token inválido para cambio de contraseña: " + request.token());
                    return PasswordResetResponse.error("Enlace inválido.");
                }
            }

            // 2. Obtener usuario y cambiar contraseña
            User usuario = resetToken.getUsuario();
            String nuevaContraEncriptada = passwordEncoder.encode(request.nuevaContra());
            usuario.setPassword(nuevaContraEncriptada);

            userRepository.save(usuario);

            // 3. Marcar token como usado
            resetToken.marcarComoUsado();
            passwordResetTokenRepository.save(resetToken);

            System.out.println("✅ Contraseña cambiada exitosamente para usuario: " + usuario.getEmail());

            return PasswordResetResponse.exito(
                    "Contraseña cambiada exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.",
                    usuario.getEmail()
            );

        } catch (Exception e) {
            System.err.println("❌ Error al cambiar contraseña con token: " + request.token());
            e.printStackTrace();
            return PasswordResetResponse.error("Error interno del servidor. Inténtalo más tarde.");
        }
    }

    /**
     * Limpia tokens expirados (método de mantenimiento)
     * ✅ SIMPLIFICADO: Solo para uso manual/programado
     */
    @Transactional
    public void limpiarTokensExpirados() {
        try {
            System.out.println("🧹 Iniciando limpieza manual de tokens expirados");
            int tokensEliminados = passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            System.out.println("✅ Limpieza manual completada - " + tokensEliminados + " tokens eliminados");
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar tokens expirados");
            e.printStackTrace();
        }
    }
}