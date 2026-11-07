package com.conicream.alpaso.application.port.out;

import com.conicream.alpaso.domain.model.Pedido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {
    Pedido guardar(Pedido pedido);

    Optional<Pedido> buscarPorId(long id);

    /** Todos los pedidos de esa fecha, en cualquier estado. */
    List<Pedido> buscarPorFecha(LocalDate fecha);
}
