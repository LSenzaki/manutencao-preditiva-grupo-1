package br.edu.biopark.manutencao.repository;

import br.edu.biopark.manutencao.domain.Predicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredicaoRepository extends JpaRepository<Predicao, Long> {

    List<Predicao> findTop50ByMaquinaIdOrderByCriadoEmDesc(String maquinaId);

    List<Predicao> findTop50ByOrderByCriadoEmDesc();
}
