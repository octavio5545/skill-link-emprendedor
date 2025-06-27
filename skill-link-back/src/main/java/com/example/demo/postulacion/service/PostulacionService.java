package com.example.demo.postulacion.service;

import com.example.demo.postulacion.model.Postulacion;
import com.example.demo.postulacion.repository.PostulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostulacionService {

    private final PostulacionRepository repository;

    @Autowired
    public PostulacionService(PostulacionRepository repository) {
        this.repository = repository;
    }

    public List<Postulacion> findAll() {
        return repository.findAll();
    }

    public Optional<Postulacion> findById(Long id) {
        return repository.findById(id);
    }

    public Postulacion save(Postulacion postulacion) {
        return repository.save(postulacion);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
