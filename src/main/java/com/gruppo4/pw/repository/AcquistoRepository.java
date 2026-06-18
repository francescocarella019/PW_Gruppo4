package com.gruppo4.pw.repository;

public interface AcquistoRepository extends CrudRepository<Acquisto, Long> {
    List<Acquisto> findByUtenteId(Long utenteId);    
}
