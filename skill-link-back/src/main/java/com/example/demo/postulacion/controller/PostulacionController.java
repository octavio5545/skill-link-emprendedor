package com.example.demo.postulacion.controller;

import com.example.demo.postulacion.dto.PostulacionesResponseDTO;
import com.example.demo.postulacion.dto.PostulacionesRequestDTO;
import com.example.demo.postulacion.service.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/postulaciones")
public class PostulacionController {

    private final PostulacionService service;

    @Autowired
    public PostulacionController(PostulacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PostulacionesResponseDTO> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Optional<PostulacionesResponseDTO> obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public PostulacionesResponseDTO crear(@RequestBody PostulacionesRequestDTO dto) {
        return service.save(dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.deleteById(id);
    }
}
