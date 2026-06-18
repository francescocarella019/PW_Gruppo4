package com.gruppo4.pw.repository;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {
    List<Recensione> findByUtenteId(Long utenteId);  
}
