package pe.edu.upeu.asistencia.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.model.Horario;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.service.HorarioService;
import pe.edu.upeu.asistencia.service.UsuarioService;

@Component
public class UsuarioFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private ComboBox<Horario> cmbHorario;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblHorarioInfo;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HorarioService horarioService;

    private Usuario usuarioActual;
    private Runnable onGuardarCallback;

    @FXML
    public void initialize() {
        // Configurar ComboBox de Roles
        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));

        // Cargar horarios activos
        cargarHorarios();

        // Listener para mostrar/ocultar horario según el rol
        cmbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == Rol.EMPLEADO) {
                cmbHorario.setDisable(false);
                lblHorarioInfo.setVisible(true);
            } else {
                cmbHorario.setDisable(true);
                cmbHorario.setValue(null);
                lblHorarioInfo.setVisible(false);
            }
        });

        // Mostrar información del horario seleccionado
        cmbHorario.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblHorarioInfo.setText("Horario: " + newVal.getHoraEntrada() + " - " + newVal.getHoraSalida());
            } else {
                lblHorarioInfo.setText("");
            }
        });
    }

    private void cargarHorarios() {
        cmbHorario.setItems(FXCollections.observableArrayList(horarioService.listarActivos()));
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtUsername.setText(usuario.getUsername());
            cmbRol.setValue(usuario.getRol());
            chkActivo.setSelected(usuario.getActivo());
            cmbHorario.setValue(usuario.getHorario());
            txtPassword.setPromptText("Dejar vacío para mantener contraseña actual");
        } else {
            chkActivo.setSelected(true);
        }
    }

    public void setOnGuardar(Runnable callback) {
        this.onGuardarCallback = callback;
    }

    @FXML
    private void handleGuardar() {
        // Validaciones
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "El nombre es obligatorio", Alert.AlertType.ERROR);
            return;
        }

        if (txtUsername.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "El usuario es obligatorio", Alert.AlertType.ERROR);
            return;
        }

        if (usuarioActual == null && txtPassword.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "La contraseña es obligatoria para usuarios nuevos", Alert.AlertType.ERROR);
            return;
        }

        if (cmbRol.getValue() == null) {
            mostrarAlerta("Error", "Debe seleccionar un rol", Alert.AlertType.ERROR);
            return;
        }

        // Validar horario para empleados
        if (cmbRol.getValue() == Rol.EMPLEADO && cmbHorario.getValue() == null) {
            mostrarAlerta("Error", "Debe asignar un horario para los empleados", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (usuarioActual == null) {
                // Nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(txtNombre.getText().trim());
                nuevoUsuario.setUsername(txtUsername.getText().trim());
                nuevoUsuario.setPasswordHash(txtPassword.getText().trim());
                nuevoUsuario.setRol(cmbRol.getValue());
                nuevoUsuario.setActivo(chkActivo.isSelected());
                nuevoUsuario.setHorario(cmbHorario.getValue());

                usuarioService.guardarUsuario(nuevoUsuario);
                mostrarAlerta("Éxito", "Usuario creado correctamente", Alert.AlertType.INFORMATION);
            } else {
                // Actualizar usuario
                usuarioActual.setNombre(txtNombre.getText().trim());
                usuarioActual.setUsername(txtUsername.getText().trim());
                usuarioActual.setRol(cmbRol.getValue());
                usuarioActual.setActivo(chkActivo.isSelected());
                usuarioActual.setHorario(cmbHorario.getValue());

                String nuevaPassword = txtPassword.getText().trim();
                usuarioService.actualizarUsuario(usuarioActual, nuevaPassword);
                mostrarAlerta("Éxito", "Usuario actualizado correctamente", Alert.AlertType.INFORMATION);
            }

            if (onGuardarCallback != null) {
                onGuardarCallback.run();
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el usuario: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        if (onGuardarCallback != null) {
            onGuardarCallback.run();
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}