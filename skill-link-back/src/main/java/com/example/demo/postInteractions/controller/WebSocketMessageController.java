package com.example.demo.postInteractions.controller;

import com.example.demo.postInteractions.dto.CommentDTO;
import com.example.demo.postInteractions.dto.ReactionNotificationDTO;
import com.example.demo.postInteractions.model.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notifica a los suscriptores sobre un nuevo comentario.
     * Este método es llamado desde CommentService.
     *
     * @param commentDTO El CommentDTO ya preparado para enviar al frontend.
     */
    public void notifyNewComment(CommentDTO commentDTO) {
        // El destino del mensaje es específico del post al que pertenece el comentario
        String destination = "/topic/comments/new";
        messagingTemplate.convertAndSend(destination, commentDTO);
    }

    /**
     * Notifica a los suscriptores sobre un cambio en las reacciones de un post o comentario.
     * CAMBIO IMPORTANTE: Ya no enviamos userReaction específica, solo los conteos generales.
     * Cada cliente deberá consultar su propia reacción individualmente.
     *
     * @param targetId      El ID del post o comentario afectado (Long).
     * @param targetType    El tipo de objetivo (POST o COMMENT).
     * @param reactionCounts Un mapa con los conteos de cada tipo de reacción.
     */
    public void notifyReactionChange(Long targetId, TargetType targetType, Map<String, Long> reactionCounts) {
        // Definimos un tópico general para todas las notificaciones de reacciones.
        // Esto simplifica la suscripción en el frontend, que solo necesita escuchar un canal.
        String destination = "/topic/reactions/new"; // Tópico general

        // Creamos una instancia de nuestro DTO para enviar la notificación
        ReactionNotificationDTO notification = new ReactionNotificationDTO(
                String.valueOf(targetId),
                targetType,
                reactionCounts,
                null
        );
        messagingTemplate.convertAndSend(destination, notification);
    }
}