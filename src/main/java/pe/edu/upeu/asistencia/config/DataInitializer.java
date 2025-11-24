package pe.edu.upeu.asistencia.config;

import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.service.HorarioService;
import pe.edu.upeu.asistencia.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final HorarioService horarioService;

    public DataInitializer(UsuarioService usuarioService, HorarioService horarioService) {
        this.usuarioService = usuarioService;
        this.horarioService = horarioService;
    }

    @Override
    public void run(String... args) {
        // Crear horarios de ejemplo
        if (horarioService.listarTodos().isEmpty()) {
            horarioService.crearHorario("Turno Mañana", LocalTime.of(8, 0), LocalTime.of(14, 0), 15);
            horarioService.crearHorario("Turno Tarde", LocalTime.of(14, 0), LocalTime.of(20, 0), 15);
            horarioService.crearHorario("Turno Completo", LocalTime.of(8, 0), LocalTime.of(17, 0), 15);
            System.out.println("✓ Horarios de ejemplo creados");
        }

        // Crear usuario administrador
        if (!usuarioService.existeUsername("admin")) {
            usuarioService.crearUsuario(
                    "Administrador del Sistema",
                    "admin",
                    "admin123",
                    Rol.ADMIN,
                    null // Admin no necesita horario
            );
            System.out.println("✓ Usuario administrador creado:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
        }

        // Crear empleado de ejemplo
        if (!usuarioService.existeUsername("empleado1")) {
            usuarioService.crearUsuario(
                    "Cristian Ccanahuire Ttito",
                    "empleado1",
                    "123456",
                    Rol.EMPLEADO,
                    horarioService.listarActivos().get(0) // Asignar primer horario
            );
            System.out.println(" Usuario empleado de prueba creado:");
            System.out.println("   empleado1");
            System.out.println("   123456");
        }
    }
}