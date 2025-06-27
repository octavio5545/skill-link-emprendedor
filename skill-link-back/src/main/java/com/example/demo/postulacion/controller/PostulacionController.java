package com.example.demo.postulacion.controller;

import com.example.demo.postulacion.model.Postulacion;
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
    public List<Postulacion> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Postulacion> obtener(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Postulacion crear(@RequestBody Postulacion postulacion) {
        return service.save(postulacion);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.deleteById(id);
    }
}
