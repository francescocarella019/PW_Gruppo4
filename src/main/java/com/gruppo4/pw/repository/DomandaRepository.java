package com.gruppo4.pw.repository;

public interface DomandaRepository extends CrudRepository<Domanda, Long> {
    List<Domanda> findByUtenteId(Long utenteId);
    
}
