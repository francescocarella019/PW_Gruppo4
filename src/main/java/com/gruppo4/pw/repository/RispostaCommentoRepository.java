package com.gruppo4.pw.repository;

public interface RispostaCommentoRepository extends CrudRepository<RispostaCommento, Long> {
    List<RispostaCommento> findByCommentoId(Long commentoId);
    
}
