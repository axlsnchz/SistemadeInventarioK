package mx.edu.utez.inventario.controller;

import mx.edu.utez.inventario.dto.VentaRequest;
import mx.edu.utez.inventario.dto.VentaResponse;
import mx.edu.utez.inventario.service.VentaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @PostMapping
    public VentaResponse crear(@RequestBody VentaRequest req, Authentication auth) {
        return VentaResponse.de(service.crear(req, auth.getName()));
    }

    @GetMapping
    public List<VentaResponse> listar(Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.listar(auth.getName(), esAdmin).stream()
                .map(VentaResponse::de).toList();
    }
}
