package com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper;

import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.ProductoEntity;

/** Traduce limpiamente entre Producto (Dominio) y ProductoEntity (JPA). */
public final class ProductoMapper {

    private ProductoMapper() { }

    public static Producto aDominio(ProductoEntity e) {
        return Producto.reconstruir(
                e.getId(),
                e.getCodigo(),
                e.getNombre(),
                e.getMarca(),
                Money.de(e.getPrecio()),
                e.isActivo()
        );
    }

    public static void copiarA(ProductoEntity e, Producto p) {
        e.setCodigo(p.getCodigo());
        e.setNombre(p.getNombre());
        e.setMarca(p.getMarca());
        e.setPrecio(p.getPrecio().valor());
        e.setActivo(p.isActivo());
    }
}
