package com.conicream.alpaso.infrastructure.adapters.out.persistence.mapper;

import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.vo.Money;
import com.conicream.alpaso.infrastructure.adapters.out.persistence.entity.ProductoEntity;
import java.util.Locale;

/** Traduce entre el modelo de dominio y el de base de datos. Ninguno de los dos conoce al otro. */
public final class ProductoMapper {

    private ProductoMapper() { }

    public static Producto aDominio(ProductoEntity e) {
        return Producto.reconstruir(e.getId(), e.getNombre(), e.getMarca(), Money.de(e.getPrecio()),
                e.getUnidadesPorCaja(), e.isActivo());
    }

    public static String clave(String nombre, String marca) {
        return nombre.trim().toLowerCase(Locale.ROOT) + "|" + marca.trim().toLowerCase(Locale.ROOT);
    }

    /** Copia el estado del dominio sobre una entidad (nueva o ya cargada, para conservar su @Version). */
    public static void copiarA(ProductoEntity e, Producto p) {
        e.setNombre(p.getNombre());
        e.setMarca(p.getMarca());
        e.setClaveActiva(p.isActivo() ? clave(p.getNombre(), p.getMarca()) : null);
        e.setPrecio(p.getPrecio().valor());
        e.setUnidadesPorCaja(p.getUnidadesPorCaja());
        e.setActivo(p.isActivo());
    }
}
