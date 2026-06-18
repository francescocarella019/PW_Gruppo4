package com.gruppo4.pw.repository;

public interface ImmagineRepository extends CrudRepository<Immagine, Long> {
    List<Immagine> findByProdottoId(Long prodottoId);
    
}
