package mx.edu.utez.inventario.dto;

import mx.edu.utez.inventario.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(Long id, LocalDateTime fecha, BigDecimal total,
                            String vendedor, List<Item> items) {
    public record Item(String producto, int cantidad, BigDecimal precioUnitario) {}

    public static VentaResponse de(Venta v) {
        return new VentaResponse(
                v.getId(), v.getFecha(), v.getTotal(), v.getUsuario().getUsername(),
                v.getDetalles().stream()
                        .map(d -> new Item(d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario()))
                        .toList());
    }
}