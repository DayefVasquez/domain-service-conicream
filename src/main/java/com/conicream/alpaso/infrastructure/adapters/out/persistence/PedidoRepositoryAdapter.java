package com.conicream.alpaso.infrastructure.adapters.out.persistence;

import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.domain.model.Pedido;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.PedidoEntity;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper.PedidoMapper;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.repository.PedidoJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository jpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        PedidoEntity entidad = pedido.getId() == null
                ? PedidoEntity.nueva()
                : jpa.findById(pedido.getId()).orElseGet(PedidoEntity::nueva);
        PedidoMapper.copiarA(entidad, pedido);
        return PedidoMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(long id) {
        return jpa.findById(id).map(PedidoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> buscarPorFecha(LocalDate fecha) {
        return jpa.findByFechaOrderByIdAsc(fecha).stream().map(PedidoMapper::aDominio).toList();
    }
}
