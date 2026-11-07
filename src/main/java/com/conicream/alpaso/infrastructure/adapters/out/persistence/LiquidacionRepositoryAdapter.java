package com.conicream.alpaso.infrastructure.adapters.out.persistence;

import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.domain.model.Liquidacion;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.LiquidacionEntity;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper.LiquidacionMapper;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.repository.LiquidacionJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class LiquidacionRepositoryAdapter implements LiquidacionRepositoryPort {

    private final LiquidacionJpaRepository jpa;

    public LiquidacionRepositoryAdapter(LiquidacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Liquidacion guardar(Liquidacion liquidacion) {
        LiquidacionEntity entidad = liquidacion.getId() == null
                ? LiquidacionEntity.nueva()
                : jpa.findById(liquidacion.getId()).orElseGet(LiquidacionEntity::nueva);
        LiquidacionMapper.copiarA(entidad, liquidacion);
        return LiquidacionMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Liquidacion> buscarPorPedido(long pedidoId) {
        return jpa.findByPedidoId(pedidoId).map(LiquidacionMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Liquidacion> buscarPorPedidos(Collection<Long> pedidoIds) {
        if (pedidoIds.isEmpty()) {
            return List.of();
        }
        return jpa.findByPedidoIdIn(pedidoIds).stream().map(LiquidacionMapper::aDominio).toList();
    }
}
