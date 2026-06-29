package com.gruppo4.pw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.TipologiaEsperienza;

public interface TipologiaEsperienzaRepository extends CrudRepository<TipologiaEsperienza, Long> {
    List<TipologiaEsperienza> findByEsperienzaId(Long esperienzaId);
}
