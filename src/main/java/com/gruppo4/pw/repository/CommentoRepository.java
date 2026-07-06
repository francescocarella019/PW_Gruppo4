package com.gruppo4.pw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.Commento;

public interface CommentoRepository extends CrudRepository<Commento, Long> {
    List<Commento> findByRecensioneId(Long recensioneId);
}
