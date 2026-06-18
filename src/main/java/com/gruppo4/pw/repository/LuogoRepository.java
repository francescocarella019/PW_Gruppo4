package com.gruppo4.pw.repository;

public interface LuogoRepository extends CrudRepository<Luogo, Long> {
    List<Luogo> findByNome(String nome);
    
}
