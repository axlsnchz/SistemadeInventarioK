package mx.edu.utez.inventario.service;

import mx.edu.utez.inventario.dto.ItemVentaRequest;
import mx.edu.utez.inventario.dto.VentaRequest;
import mx.edu.utez.inventario.model.DetalleVenta;
import mx.edu.utez.inventario.model.Producto;
import mx.edu.utez.inventario.model.Usuario;
import mx.edu.utez.inventario.model.Venta;
import mx.edu.utez.inventario.repository.ProductoRepository;
import mx.edu.utez.inventario.repository.UsuarioRepository;
import mx.edu.utez.inventario.repository.VentaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public VentaService(VentaRepository ventaRepo, ProductoRepository productoRepo,
                        UsuarioRepository usuarioRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public Venta crear(VentaRequest req, String username) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La venta no tiene productos");
        }
        Usuario usuario = usuarioRepo.findByUsername(username).orElseThrow();
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        BigDecimal total = BigDecimal.ZERO;

        for (ItemVentaRequest item : req.items()) {
            if (item.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
            }
            Producto p = productoRepo.findById(item.productoId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            if (p.getStock() < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente de " + p.getNombre());
            }
            p.setStock(p.getStock() - item.cantidad());

            DetalleVenta d = new DetalleVenta();
            d.setVenta(venta);
            d.setProducto(p);
            d.setCantidad(item.cantidad());
            d.setPrecioUnitario(p.getPrecio());
            venta.getDetalles().add(d);

            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
        }
        venta.setTotal(total);
        return ventaRepo.save(venta);
    }

    @Transactional(readOnly = true)
    public List<Venta> listar(String username, boolean esAdmin) {
        if (esAdmin) return ventaRepo.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepo.findByUsuarioUsernameOrderByFechaDesc(username);
    }
}
