package com.gruppo4.pw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.Risposta;

public interface RispostaRepository extends CrudRepository<Risposta, Long> {
    List<Risposta> findByDomandaId(Long domandaId);
}
