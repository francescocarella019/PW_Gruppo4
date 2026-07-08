package com.gruppo4.pw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.gruppo4.pw.model.Immagine;

public interface ImmagineRepository extends CrudRepository<Immagine, Long> {
    List<Immagine> findByRecensioneId(Long recensioneId);
}
