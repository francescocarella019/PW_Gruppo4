package com.gruppo4.pw.repository;

public interface UtenteRepository extends CrudRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
}
