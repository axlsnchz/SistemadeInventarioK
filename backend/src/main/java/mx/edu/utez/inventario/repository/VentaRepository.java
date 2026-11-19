package mx.edu.utez.inventario.repository;

import mx.edu.utez.inventario.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Long> {
}
