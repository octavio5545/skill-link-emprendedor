package com.example.demo.postulacion.service;

import com.example.demo.postulacion.dto.PostulacionesRequestDTO;
import com.example.demo.postulacion.dto.PostulacionesResponseDTO;
import com.example.demo.postulacion.model.Postulacion;
import com.example.demo.postulacion.repository.PostulacionRepository;
import com.example.demo.proyectonegocio.model.ProyectoNegocio;
import com.example.demo.proyectonegocio.model.repository.ProyectoNegocioRepository;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostulacionService {

    private final PostulacionRepository repository;
    private final ProyectoNegocioRepository proyectoRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostulacionService(PostulacionRepository repository,
                              ProyectoNegocioRepository proyectoRepository,
                              UserRepository userRepository) {
        this.repository = repository;
        this.proyectoRepository = proyectoRepository;
        this.userRepository = userRepository;
    }

    public List<PostulacionesResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(p -> new PostulacionesResponseDTO(
                        p.getId(),
                        p.getProject().getId(),
                        String.valueOf(p.getParticipant().getId()),
                        p.getParticipant().getName(),
                        p.getApplicationDate(),
                        p.getResponseDate(),
                        p.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public Optional<PostulacionesResponseDTO> findById(Long id) {
        return repository.findById(id)
                .map(p -> new PostulacionesResponseDTO(
                        p.getId(),
                        p.getProject().getId(),
                        String.valueOf(p.getParticipant().getId()),
                        p.getParticipant().getName(),
                        p.getApplicationDate(),
                        p.getResponseDate(),
                        p.getStatus()
                ));
    }

    public PostulacionesResponseDTO save(PostulacionesRequestDTO dto) {
        ProyectoNegocio proyecto = proyectoRepository.findById(dto.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User user = userRepository.findById(dto.participantId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Postulacion nueva = new Postulacion(proyecto, user);
        Postulacion guardada = repository.save(nueva);

        return new PostulacionesResponseDTO(
                guardada.getId(),
                proyecto.getId(),
                String.valueOf(user.getId()),
                user.getName(),
                guardada.getApplicationDate(),
                guardada.getResponseDate(),
                guardada.getStatus()
        );
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
