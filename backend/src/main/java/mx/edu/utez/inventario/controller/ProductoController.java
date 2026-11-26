package mx.edu.utez.inventario.controller;

import mx.edu.utez.inventario.model.Producto;
import mx.edu.utez.inventario.service.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String q) {
        return service.listar(q);
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/nfc/{nfcId}")
    public Producto porNfc(@PathVariable String nfcId) {
        return service.porNfc(nfcId);
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        return service.crear(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto foto(@PathVariable Long id, @RequestPart("image") MultipartFile image)
            throws IOException {
        return service.guardarFoto(id, image);
    }
}
