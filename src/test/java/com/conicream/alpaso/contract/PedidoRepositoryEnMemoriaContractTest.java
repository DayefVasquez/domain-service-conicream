package com.conicream.alpaso.contract;

import com.conicream.alpaso.application.port.out.PedidoRepositoryPort;
import com.conicream.alpaso.testsupport.PedidoRepositoryEnMemoria;

class PedidoRepositoryEnMemoriaContractTest extends PedidoRepositoryContractTest {
    private final PedidoRepositoryEnMemoria repo = new PedidoRepositoryEnMemoria();
    private long seq = 0;
    @Override protected PedidoRepositoryPort repo() { return repo; }
    @Override protected long crearProducto(String nombre) { return ++seq; }
}
