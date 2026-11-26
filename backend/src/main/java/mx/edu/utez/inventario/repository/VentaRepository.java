package mx.edu.utez.inventario.repository;

import mx.edu.utez.inventario.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByUsuarioUsernameOrderByFechaDesc(String username);
}
