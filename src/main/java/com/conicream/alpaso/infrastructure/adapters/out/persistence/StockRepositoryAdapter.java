package com.conicream.alpaso.infrastructure.adapters.out.persistence;

import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.domain.model.StockProducto;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.StockEntity;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper.StockMapper;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.repository.StockJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class StockRepositoryAdapter implements StockRepositoryPort {

    private final StockJpaRepository jpa;

    public StockRepositoryAdapter(StockJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public StockProducto guardar(StockProducto stock) {
        StockEntity entidad = jpa.findById(stock.getProductoId()).orElseGet(StockEntity::nueva);
        StockMapper.copiarA(entidad, stock);
        return StockMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockProducto> buscarPorProducto(long productoId) {
        return jpa.findById(productoId).map(StockMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockProducto> buscarPorProductos(Collection<Long> productoIds) {
        return jpa.findAllById(productoIds).stream().map(StockMapper::aDominio).toList();
    }
}
