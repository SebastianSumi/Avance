package pe.edu.upeu.asistencia.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.service.UsuarioService;

@Component
public class UsuarioFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    @Autowired
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Runnable onGuardar;

    @FXML
    public void initialize() {
        cmbRol.getItems().addAll(Rol.values());
        cmbRol.setValue(Rol.EMPLEADO);
        chkActivo.setSelected(true);
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtUsername.setText(usuario.getUsername());
            txtUsername.setDisable(true); // No permitir cambiar username
            cmbRol.setValue(usuario.getRol());
            chkActivo.setSelected(usuario.getActivo());
            txtPassword.setPromptText("Dejar vacío para mantener contraseña actual");
        }
    }

    public void setOnGuardar(Runnable callback) {
        this.onGuardar = callback;
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            if (usuario == null) {
                // Nuevo usuario
                usuario = new Usuario();
                usuario.setNombre(txtNombre.getText().trim());
                usuario.setUsername(txtUsername.getText().trim());
                usuario.setPasswordHash(txtPassword.getText()); // Se encriptará en el servicio
                usuario.setRol(cmbRol.getValue());
                usuario.setActivo(chkActivo.isSelected());

                usuarioService.guardarUsuario(usuario);
            } else {
                // Editar usuario
                usuario.setNombre(txtNombre.getText().trim());
                usuario.setRol(cmbRol.getValue());
                usuario.setActivo(chkActivo.isSelected());

                String nuevaPassword = txtPassword.getText();
                usuarioService.actualizarUsuario(usuario, nuevaPassword.isEmpty() ? null : nuevaPassword);
            }

            if (onGuardar != null) {
                onGuardar.run();
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el usuario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        btnCancelar.getScene().getWindow().hide();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING);
            return false;
        }
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El username es obligatorio", Alert.AlertType.WARNING);
            return false;
        }
        if (usuario == null && txtPassword.getText().isEmpty()) {
            mostrarAlerta("Validación", "La contraseña es obligatoria para nuevos usuarios", Alert.AlertType.WARNING);
            return false;
        }
        if (cmbRol.getValue() == null) {
            mostrarAlerta("Validación", "Debe seleccionar un rol", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}