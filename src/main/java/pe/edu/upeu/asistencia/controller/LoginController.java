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
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioService.autenticar(username, password);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            SessionManager.getInstance().setUsuarioActual(usuario);

            // Cambiar según el rol
            if (usuario.getRol() == Rol.ADMIN) {
                stageManager.cambiarEscena("/fxml/admin_dashboard.fxml", "Panel de Administración", 900, 600);
            } else {
                stageManager.cambiarEscena("/fxml/empleado_dashboard.fxml", "Panel de Empleado", 900, 600);
            }
        } else {
            mostrarError("Usuario o contraseña incorrectos");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setStyle("-fx-text-fill: red;");
    }
} 