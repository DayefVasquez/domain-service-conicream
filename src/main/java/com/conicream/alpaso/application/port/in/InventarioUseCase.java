package com.conicream.alpaso.application.port.in;

import com.conicream.alpaso.application.port.in.command.CorregirStockCommand;
import com.conicream.alpaso.application.port.in.command.ReponerStockCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;

public interface InventarioUseCase {
    ProductoConStock consultar(long productoId);

    ProductoConStock reponer(ReponerStockCommand comando);

    ProductoConStock corregir(CorregirStockCommand comando);
}
