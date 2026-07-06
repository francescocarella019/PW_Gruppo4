package com.gruppo4.pw.repository;

import com.gruppo4.pw.model.Contatto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContattoRepository extends JpaRepository<Contatto, Long> {
    // Estendendo JpaRepository hai già a disposizione i metodi CRUD (save, findAll, delete, ecc.)
}