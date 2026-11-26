package mx.edu.utez.inventario;

import mx.edu.utez.inventario.model.Rol;
import mx.edu.utez.inventario.model.Usuario;
import mx.edu.utez.inventario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public DataInitializer(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        crear("admin", "admin123", Rol.ADMIN);
        crear("vendedor", "vendedor123", Rol.VENDEDOR);
    }

    private void crear(String username, String password, Rol rol) {
        if (repo.findByUsername(username).isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(encoder.encode(password));
            u.setRol(rol);
            repo.save(u);
        }
    }
}
