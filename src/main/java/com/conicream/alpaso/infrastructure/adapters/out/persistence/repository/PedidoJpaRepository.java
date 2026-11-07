package com.conicream.alpaso.infrastructure.adapters.out.persistence.repository;

import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.PedidoEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByFechaOrderByIdAsc(LocalDate fecha);
}
