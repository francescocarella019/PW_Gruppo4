package com.gruppo4.pw.repository;

public interface CommentoRepository extends CrudRepository<Commento, Long> {
    List<Commento> findByUtenteId(Long utenteId);
    
}
