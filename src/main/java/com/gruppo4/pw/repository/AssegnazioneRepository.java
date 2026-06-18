package com.gruppo4.pw.repository;

public interface AssegnazioneRepository extends CrudRepository<Assegnazione, Long> {
    List<Assegnazione> findByUtenteId(Long utenteId);
    
}
