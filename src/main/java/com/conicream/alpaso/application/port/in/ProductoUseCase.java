package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.command.EditarProductoCommand;
import com.conicream.alpaso.application.port.in.command.RegistrarProductoCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import java.util.List;

public interface ProductoUseCase {
    ProductoConStock registrar(RegistrarProductoCommand comando);

    ProductoConStock editar(EditarProductoCommand comando);

    /** Baja lógica: el producto queda inactivo, no se borra. */
    ProductoConStock retirar(long id);

    ProductoConStock porId(long id);

    List<ProductoConStock> listar(boolean soloActivos);
}
