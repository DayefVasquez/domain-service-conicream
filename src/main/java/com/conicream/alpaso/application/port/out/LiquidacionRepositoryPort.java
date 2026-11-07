package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.Liquidacion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LiquidacionRepositoryPort {
    Liquidacion guardar(Liquidacion liquidacion);

    Optional<Liquidacion> buscarPorPedido(long pedidoId);

    List<Liquidacion> buscarPorPedidos(Collection<Long> pedidoIds);
}
