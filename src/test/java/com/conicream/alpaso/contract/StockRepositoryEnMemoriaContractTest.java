package com.conicream.alpaso.contract;

import com.conicream.alpaso.application.port.out.StockRepositoryPort;
import com.conicream.alpaso.testsupport.StockRepositoryEnMemoria;

class StockRepositoryEnMemoriaContractTest extends StockRepositoryContractTest {
    private final StockRepositoryEnMemoria repo = new StockRepositoryEnMemoria();
    private long seq = 0;
    @Override protected StockRepositoryPort repo() { return repo; }
    @Override protected long crearProducto(String nombre) { return ++seq; }
}
