package com.gruppo4.pw.repository;

import com.gruppo4.pw.model.Recensione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecensioneRepository extends JpaRepository<Recensione, Long> {
    
    List<Recensione> findByEsperienzaId(Long esperienzaId);
    
    // Query JPQL esplicita passante per l'entità intermedia TipologiaEsperienza
    @Query("SELECT r FROM Recensione r JOIN TipologiaEsperienza te ON te.esperienza = r.esperienza WHERE te.tipologia.id = :categoriaId")
    List<Recensione> findByCategoriaIdPersonalizzato(@Param("categoriaId") Long categoriaId);
}