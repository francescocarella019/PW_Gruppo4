package com.gruppo4.pw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.Domanda;

public interface DomandaRepository extends CrudRepository<Domanda, Long> {
    
}
