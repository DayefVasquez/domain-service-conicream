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
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpa;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Producto guardar(Producto producto) {
        // Si ya existe se reutiliza la entidad cargada para conservar su @Version.
        ProductoEntity entidad = producto.getId() == null
                ? ProductoEntity.nueva()
                : jpa.findById(producto.getId()).orElseGet(ProductoEntity::nueva);
        ProductoMapper.copiarA(entidad, producto);
        return ProductoMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(long id) {
        return jpa.findById(id).map(ProductoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> buscarActivoPorNombreYMarca(String nombre, String marca) {
        return jpa.findByClaveActiva(ProductoMapper.clave(nombre, marca)).map(ProductoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(ProductoMapper::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar(boolean soloActivos) {
        List<ProductoEntity> filas = soloActivos ? jpa.findByActivoTrueOrderByIdAsc() : jpa.findAllByOrderByIdAsc();
        return filas.stream().map(ProductoMapper::aDominio).toList();
    }
}
