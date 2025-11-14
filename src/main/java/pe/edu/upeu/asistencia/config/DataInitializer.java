package pe.edu.upeu.asistencia.config;

import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    //aqui el DataInitializer crea un usuario administrador la primera vez para que la BD no este vacia , algo estatico

    public DataInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (!usuarioService.existeUsername("admin")) {
            usuarioService.crearUsuario(
                    "Administrador del Sistema",
                    "admin",
                    "admin123",
                    Rol.ADMIN
            );
            System.out.println("✓ Usuario administrador creado:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
        }

        if (!usuarioService.existeUsername("empleado1")) {
            usuarioService.crearUsuario(
                    "Juan Pérez López",
                    "empleado1",
                    "123456",
                    Rol.EMPLEADO
            );
            System.out.println("✓ Usuario empleado de prueba creado:");
            System.out.println("  Username: empleado1");
            System.out.println("  Password: 123456");
        }
    }
}