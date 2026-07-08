package com.gruppo4.pw.repository;

import com.gruppo4.pw.model.TipologiaEsperienza;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface TipologiaEsperienzaRepository extends CrudRepository<TipologiaEsperienza, Long> {
    List<TipologiaEsperienza> findByEsperienzaId(Long esperienzaId);
    
    // AGGIUNGI QUESTO METODO SE MANCA:
    List<TipologiaEsperienza> findByTipologiaId(Long tipologiaId); 
}