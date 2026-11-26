package mx.edu.utez.inventario.controller;

import mx.edu.utez.inventario.model.Usuario;
import mx.edu.utez.inventario.repository.UsuarioRepository;
import mx.edu.utez.inventario.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username, String rol) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Usuario u = repo.findByUsername(req.username()).orElse(null);
        if (u == null || !u.isActivo() || !encoder.matches(req.password(), u.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
        }
        String token = jwtService.generarToken(u.getUsername(), u.getRol());
        return ResponseEntity.ok(new LoginResponse(token, u.getUsername(), u.getRol().name()));
    }
}
