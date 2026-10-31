package com.conicream.alpaso.application.port.in.result;

import com.conicream.alpaso.domain.model.Producto;
import com.conicream.alpaso.domain.model.StockProducto;

public record ProductoConStock(Producto producto, StockProducto stock) { }
