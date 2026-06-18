package com.gruppo4.pw.repository;

public interface TipologiaRepository extends CrudRepository<Tipologia, Long> {
    List<Tipologia> findByNome(String nome);
    
}
