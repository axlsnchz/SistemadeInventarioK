package mx.edu.utez.inventario.dto;

import java.util.List;

public record VentaRequest(List<ItemVentaRequest> items) {
    
}
