package com.example.demo.postulacion.repository;

import com.example.demo.postulacion.model.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
}
