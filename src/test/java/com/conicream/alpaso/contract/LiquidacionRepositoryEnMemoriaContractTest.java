package com.conicream.alpaso.contract;

import com.conicream.alpaso.application.port.out.LiquidacionRepositoryPort;
import com.conicream.alpaso.testsupport.LiquidacionRepositoryEnMemoria;

class LiquidacionRepositoryEnMemoriaContractTest extends LiquidacionRepositoryContractTest {
    private final LiquidacionRepositoryEnMemoria repo = new LiquidacionRepositoryEnMemoria();
    private long seq = 0;
    @Override protected LiquidacionRepositoryPort repo() { return repo; }
    @Override protected long crearPedido() { return ++seq; }
}
