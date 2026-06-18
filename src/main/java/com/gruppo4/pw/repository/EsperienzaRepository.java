package com.gruppo4.pw.repository;

public interface EsperienzaRepository extends CrudRepository<Esperienza, Long> {
    List<Esperienza> findByUtenteId(Long utenteId);  
}
