package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.Producto;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {
    /** Con id nulo inserta y asigna id; con id existente actualiza. */
    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(long id);

    /** Compara sin distinguir mayúsculas y solo entre productos activos. */
    Optional<Producto> buscarActivoPorNombreYMarca(String nombre, String marca);

    List<Producto> buscarPorIds(Collection<Long> ids);

    /** Ordenados por id ascendente. */
    List<Producto> listar(boolean soloActivos);
}
