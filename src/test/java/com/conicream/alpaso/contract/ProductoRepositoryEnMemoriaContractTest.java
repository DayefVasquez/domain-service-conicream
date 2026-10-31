package com.conicream.alpaso.contract;

import com.conicream.alpaso.application.port.out.ProductoRepositoryPort;
import com.conicream.alpaso.testsupport.ProductoRepositoryEnMemoria;

class ProductoRepositoryEnMemoriaContractTest extends ProductoRepositoryContractTest {
    private final ProductoRepositoryEnMemoria repo = new ProductoRepositoryEnMemoria();
    @Override protected ProductoRepositoryPort repo() { return repo; }
}
