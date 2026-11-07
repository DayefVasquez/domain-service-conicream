package com.conicream.alpaso.infrastructure.adapters.out.persistence.repository;

import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.ProductoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
    Optional<ProductoEntity> findByClaveActiva(String claveActiva);
    List<ProductoEntity> findByActivoTrueOrderByIdAsc();
    List<ProductoEntity> findAllByOrderByIdAsc();
}
