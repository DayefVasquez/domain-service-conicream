package com.conicream.alpaso.infrastructure.adapters.out.persistence.repository;

import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.LiquidacionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiquidacionJpaRepository extends JpaRepository<LiquidacionEntity, Long> {
    Optional<LiquidacionEntity> findByPedidoId(long pedidoId);
    List<LiquidacionEntity> findByPedidoIdIn(Collection<Long> pedidoIds);
}
