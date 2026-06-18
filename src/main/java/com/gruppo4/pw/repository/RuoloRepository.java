package com.gruppo4.pw.repository;

public interface RuoloRepository extends CrudRepository<Ruolo, Long> {
    Optional<Ruolo> findByNome(String nome);
}
