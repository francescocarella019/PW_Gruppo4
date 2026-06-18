package com.gruppo4.pw.repository;

public interface RispostaRepository extends CrudRepository<Risposta, Long> {
    List<Risposta> findByCommentoId(Long commentoId);
    
}
