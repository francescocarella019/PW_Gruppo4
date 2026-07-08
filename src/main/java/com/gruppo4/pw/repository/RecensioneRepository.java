package com.gruppo4.pw.repository;

import com.gruppo4.pw.model.Recensione;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {
    
    // QUESTO METODO DEVE ESSERE PRESENTE:
    List<Recensione> findByEsperienzaId(Long esperienzaId);

    List<Recensione> findByUtenteIdOrderByDataDesc(Long utenteId);
}
