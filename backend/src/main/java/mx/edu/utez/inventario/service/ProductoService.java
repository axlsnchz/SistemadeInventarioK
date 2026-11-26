package mx.edu.utez.inventario.service;

import mx.edu.utez.inventario.model.Producto;
import mx.edu.utez.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    public List<Producto> listar(String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        return repo.findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(q, q);
    }

    public Producto obtener(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Producto porNfc(String nfcId) {
        return repo.findByNfcId(nfcId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Etiqueta NFC sin producto"));
    }

    public Producto crear(Producto p) {
        p.setId(null);
        return repo.save(normalizar(p));
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto p = obtener(id);
        p.setNombre(datos.getNombre());
        p.setSku(datos.getSku());
        p.setPrecio(datos.getPrecio());
        p.setStock(datos.getStock());
        p.setStockMinimo(datos.getStockMinimo());
        p.setCategoria(datos.getCategoria());
        p.setNfcId(datos.getNfcId());
        return repo.save(normalizar(p));
    }

    public Producto guardarFoto(Long id, MultipartFile archivo) throws IOException {
        Producto p = obtener(id);
        Path dir = Path.of(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);
        String nombre = UUID.randomUUID() + ".jpg";
        archivo.transferTo(dir.resolve(nombre));
        p.setFotoUrl(nombre);
        return repo.save(p);
    }

    // Evita que "" choque con las columnas únicas (sku, nfcId)
    private Producto normalizar(Producto p) {
        if (p.getSku() != null && p.getSku().isBlank()) p.setSku(null);
        if (p.getNfcId() != null && p.getNfcId().isBlank()) p.setNfcId(null);
        return p;
    }
}
