package com.conicream.alpaso.infrastructure.adapters.out.persistence.repository;

import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> { }
