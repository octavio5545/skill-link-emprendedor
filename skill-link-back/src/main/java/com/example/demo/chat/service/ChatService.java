package com.example.demo.chat.service;

import com.example.demo.chat.dto.ConversacionResumenDTO;
import com.example.demo.chat.dto.MensajeDTO;
import com.example.demo.chat.model.Conversacion;
import com.example.demo.chat.model.Mensaje;
import com.example.demo.user.model.User;
import com.example.demo.chat.repository.ConversacionRepository;
import com.example.demo.chat.repository.MensajeRepository;
import com.example.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private UserRepository usuarioRepository;

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    // Buscar o crear conversación entre dos usuarios
    public Conversacion obtenerOCrearConversacion(Long idUsuario1, Long idUsuario2) {
        User u1 = usuarioRepository.findById(idUsuario1)
                .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
        User u2 = usuarioRepository.findById(idUsuario2)
                .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

        Optional<Conversacion> existente = conversacionRepository.findByUsuario1AndUsuario2(u1, u2);
        if (existente.isEmpty()) {
            existente = conversacionRepository.findByUsuario2AndUsuario1(u1, u2);
        }

        return existente.orElseGet(() -> {
            Conversacion nueva = new Conversacion();
            nueva.setUsuario1(u1);
            nueva.setUsuario2(u2);
            return conversacionRepository.save(nueva);
        });
    }

    public Mensaje enviarMensaje(Long idConversacion, Long idEmisor, String contenido) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));
        User emisor = usuarioRepository.findById(idEmisor)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setContenido(contenido);
        mensaje.setConversacion(conversacion);
        mensaje.setEmisor(emisor);
        mensaje.setTimestampEnvio(LocalDateTime.now());
        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);

        if (mensajeGuardado.getEmisor() == null) {
            throw new RuntimeException("Error: Mensaje guardado sin emisor");
        }
        return mensajeGuardado;
    }

    public List<Mensaje> obtenerUltimosMensajes(Long idConversacion) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        List<Mensaje> mensajes = mensajeRepository
                .findTop20ByConversacionWithEmisor(conversacion);
        mensajes.sort(Comparator.comparing(Mensaje::getTimestampEnvio));
        return mensajes;
    }

    public List<MensajeDTO> obtenerMensajesPaginados(Long idConversacion, int page, int size) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestampEnvio").descending());
        Page<Mensaje> mensajesPage = mensajeRepository.findByConversacionWithEmisorOrderByTimestampEnvioDesc(conversacion, pageable);
        // Ordenar cronológicamente (más antiguos primero)
        List<MensajeDTO> mensajesDTO = mensajesPage.getContent().stream()
                .sorted(Comparator.comparing(Mensaje::getTimestampEnvio))
                .map(mensaje -> new MensajeDTO(
                        mensaje.getId(),
                        mensaje.getContenido(),
                        mensaje.isLeido(),
                        mensaje.getTimestampEnvio(),
                        mensaje.getEmisor().getId(),
                        mensaje.getEmisor().getName(),
                        mensaje.getEmisor().getEmail()
                ))
                .collect(Collectors.toList());
        return mensajesDTO;
    }

    public List<MensajeDTO> obtenerMensajesDTO(Long idConversacion) {
        List<Mensaje> mensajes = obtenerUltimosMensajes(idConversacion);

        return mensajes.stream()
                .map(mensaje -> new MensajeDTO(
                        mensaje.getId(),
                        mensaje.getContenido(),
                        mensaje.isLeido(),
                        mensaje.getTimestampEnvio(),
                        mensaje.getEmisor().getId(),
                        mensaje.getEmisor().getName(),
                        mensaje.getEmisor().getEmail()
                ))
                .collect(Collectors.toList());
    }

    public List<Conversacion> listarConversacionesDeUsuario(Long idUsuario) {
        User usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return conversacionRepository.findConversacionesConUsuarios(usuario);
    }

    public List<ConversacionResumenDTO> listarResumenesConversacionesDeUsuario(Long idUsuario) {
        User actual = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Conversacion> conversaciones = listarConversacionesDeUsuario(idUsuario);
        if (conversaciones.isEmpty()) {
            return new ArrayList<>();
        }

        List<Mensaje> ultimosMensajes = mensajeRepository.findUltimosMensajesPorConversaciones(conversaciones);
        List<Object[]> conteoNoLeidos = mensajeRepository.countMensajesNoLeidosPorConversacion(conversaciones, actual);
        Map<Long, Mensaje> ultimoMensajePorConversacion = ultimosMensajes.stream()
                .collect(Collectors.toMap(
                        m -> m.getConversacion().getId(),
                        m -> m
                ));

        Map<Long, Long> noLeidosPorConversacion = conteoNoLeidos.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        List<ConversacionResumenDTO> resumenes = new ArrayList<>();
        for (Conversacion c : conversaciones) {
            Mensaje ultimoMensaje = ultimoMensajePorConversacion.get(c.getId());
            if (ultimoMensaje == null) continue; // Conversación sin mensajes

            User otro = c.getUsuario1().getId().equals(idUsuario)
                    ? c.getUsuario2()
                    : c.getUsuario1();

            int noLeidos = noLeidosPorConversacion.getOrDefault(c.getId(), 0L).intValue();

            resumenes.add(new ConversacionResumenDTO(
                    c.getId(),
                    otro.getId(),
                    otro.getName(),
                    otro.getEmail(),
                    ultimoMensaje.getContenido(),
                    ultimoMensaje.getTimestampEnvio(),
                    noLeidos
            ));
        }
        resumenes.sort(Comparator.comparing(ConversacionResumenDTO::getTimestampUltimoMensaje).reversed());
        return resumenes;
    }

    public void marcarMensajesComoLeidos(Long idConversacion, Long idUsuario) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        User receptor = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Mensaje> mensajesNoLeidos = mensajeRepository.findByConversacionAndEmisorNotAndLeidoFalse(
                conversacion, receptor
        );

        for (Mensaje mensaje : mensajesNoLeidos) {
            mensaje.setLeido(true);
        }
        mensajeRepository.saveAll(mensajesNoLeidos);
    }
}