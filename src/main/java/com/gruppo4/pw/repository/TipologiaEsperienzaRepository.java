package com.gruppo4.pw.repository;

public interface TipologiaEsperienzaRepository extends CrudRepository<TipologiaEsperienza, Long> {
    List<TipologiaEsperienza> findByNome(String nome);
    
}
