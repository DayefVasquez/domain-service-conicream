package com.conicream.alpaso.infrastructure.adapters.out.persistence;

import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.ProductoEntity;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper.ProductoMapper;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.repository.ProductoJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpa;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Producto guardar(Producto p) {
        ProductoEntity e = p.getId() == null
                ? ProductoEntity.nueva()
                : jpa.findById(p.getId()).orElseGet(ProductoEntity::nueva);
        ProductoMapper.copiarA(e, p);
        return ProductoMapper.aDominio(jpa.save(e));
    }

    @Override
    public Optional<Producto> buscarPorId(long id) {
        return jpa.findById(id).map(ProductoMapper::aDominio);
    }

    @Override
    public Optional<Producto> buscarActivoPorNombreYMarca(String nombre, String marca) {
        return jpa.findByNombreIgnoreCaseAndMarcaIgnoreCaseAndActivoTrue(nombre, marca)
                .map(ProductoMapper::aDominio);
    }

    @Override
    public List<Producto> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(ProductoMapper::aDominio).toList();
    }

    @Override
    public List<Producto> listar(boolean soloActivos) {
        var entidades = soloActivos ? jpa.findByActivoTrueOrderByIdAsc() : jpa.findAllByOrderByIdAsc();
        return entidades.stream().map(ProductoMapper::aDominio).toList();
    }
}
