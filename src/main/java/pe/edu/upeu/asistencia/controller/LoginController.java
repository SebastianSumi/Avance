package pe.edu.upeu.asistencia.controller;

import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.service.UsuarioService;
import pe.edu.upeu.asistencia.config.SessionManager;
import pe.edu.upeu.asistencia.config.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final UsuarioService usuarioService;
    private final StageManager stageManager;

    @Autowired
    public LoginController(UsuarioService usuarioService, StageManager stageManager) {
        this.usuarioService = usuarioService;
        this.stageManager = stageManager;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        System.out.println("INICIO LOGIN ");
        System.out.println("Login click");

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        System.out.println("Usuario ingresado: " + username);
        System.out.println("Password ingresado: " + password);

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        try {
            System.out.println("Intentando autenticar...");
            Optional<Usuario> usuarioOpt = usuarioService.autenticar(username, password);
            System.out.println("Resultado autenticación: " + usuarioOpt.isPresent());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                System.out.println("Usuario encontrado: " + usuario.getUsername());
                System.out.println("Rol: " + usuario.getRol());

                SessionManager.getInstance().setUsuarioActual(usuario);

                // Cambiar según el rol
                if (usuario.getRol() == Rol.ADMIN) {
                    System.out.println("Redirigiendo a admin dashboard...");
                    stageManager.cambiarEscena("/fxml/admin_dashboard.fxml", "Panel de Administración", 900, 600);
                } else {
                    System.out.println("Redirigiendo a empleado dashboard...");
                    stageManager.cambiarEscena("/fxml/empleado_dashboard.fxml", "Panel de Empleado", 900, 600);
                }
            } else {
                System.out.println("Autenticación fallida");
                mostrarError("Usuario o contraseña incorrectos");
            }
        } catch (Exception e) {
            System.err.println("ERROR en login: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al intentar iniciar sesión: " + e.getMessage());
        }
        System.out.println("FIN LOGIN");
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setStyle("-fx-text-fill: red;");
    }
} 