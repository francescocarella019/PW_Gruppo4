package com.gruppo4.pw.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.Utente;

public interface UtenteRepository extends CrudRepository<Utente, Long> {
    boolean existsByEmail(String email);
    Optional<Utente> findByEmail(String email);
}
