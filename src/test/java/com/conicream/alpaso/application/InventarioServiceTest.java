package com.conicream.alpaso.application;

import static org.junit.jupiter.api.Assertions.*;

import com.conicream.alpaso.application.port.in.command.CorregirStockCommand;
import com.conicream.alpaso.application.port.in.command.ReponerStockCommand;
import com.conicream.alpaso.application.port.in.result.ProductoConStock;
import com.conicream.alpaso.domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Aplicación: InventarioService")
class InventarioServiceTest {

    @Test
    @DisplayName("Repone y corrige stock en unidades directas")
    void reponerYCorregirStock() {
        Escenario e = new Escenario();
        long id = e.producto("CHOC-001", "Chococono", "Crem", 3200, 100, 20).producto().getId();

        // Reposición directa de 50 unidades
        ProductoConStock repuesto = e.inventarioService.reponer(new ReponerStockCommand(id, 50));
        assertEquals(150, repuesto.stock().unidadesDisponibles());

        // Corrección por conteo físico directo
        assertEquals(53, e.inventarioService.corregir(new CorregirStockCommand(id, 53)).stock().unidadesDisponibles());
        assertEquals(24, e.inventarioService.corregir(new CorregirStockCommand(id, 24)).stock().unidadesDisponibles());
        
        // Conteo negativo inválido
        assertThrows(ValorInvalidoException.class, () -> e.inventarioService.corregir(new CorregirStockCommand(id, -5)));
    }
}
