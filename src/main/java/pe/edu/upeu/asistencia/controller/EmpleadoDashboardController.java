package pe.edu.upeu.asistencia.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.upeu.asistencia.model.Asistencia;
import pe.edu.upeu.asistencia.model.Mensaje;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.service.AsistenciaService;
import pe.edu.upeu.asistencia.service.MensajeService;
import pe.edu.upeu.asistencia.service.UsuarioService;
import pe.edu.upeu.asistencia.config.SessionManager;
import pe.edu.upeu.asistencia.config.StageManager;
import pe.edu.upeu.asistencia.model.SolicitudVacacion;
import pe.edu.upeu.asistencia.service.SolicitudVacacionService;
import pe.edu.upeu.asistencia.enums.Rol;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class EmpleadoDashboardController {

    // Labels de información personal
    @FXML private Label lblNombre;
    @FXML private Label lblUsername;
    @FXML private Label lblRol;
    @FXML private Label lblUsuario;
    @FXML private Label lblFechaHoy;
    @FXML private Label lblHoraActual;
    @FXML private Label lblHorarioInfo;
    @FXML private Label lblEstadoAsistenciaHoy;
    @FXML private Label lblMensajesNoLeidos;

    // Botones principales
    @FXML private Button btnMarcarAsistencia;
    @FXML private Button btnRegistrarSalida;

    // ComboBox
    @FXML private ComboBox<String> cboPeriodo;

    // Tabla Mis Asistencias
    @FXML private TableView<Asistencia> tableMisAsistencias;
    @FXML private TableColumn<Asistencia, String> colMiAsistFecha;
    @FXML private TableColumn<Asistencia, String> colMiAsistEntrada;
    @FXML private TableColumn<Asistencia, String> colMiAsistSalida;
    @FXML private TableColumn<Asistencia, String> colMiAsistEstado;
    @FXML private TableColumn<Asistencia, String> colMiAsistObs;

    // Labels de estadísticas
    @FXML private Label lblTotalPresente;
    @FXML private Label lblTotalTarde;
    @FXML private Label lblTotalAusente;
    @FXML private Label lblTotalJustificado;

    // Tabla Mis Vacaciones
    @FXML private TableView<Object> tableMisVacaciones;
    @FXML private TableColumn<Object, String> colVacId;
    @FXML private TableColumn<Object, String> colVacFechaInicio;
    @FXML private TableColumn<Object, String> colVacFechaFin;
    @FXML private TableColumn<Object, String> colVacDias;
    @FXML private TableColumn<Object, String> colVacEstado;
    @FXML private TableColumn<Object, String> colVacFechaSolicitud;

    // Tabla Mis Mensajes
    @FXML private TableView<Mensaje> tableMisMensajes;
    @FXML private TableColumn<Mensaje, String> colMiMsjEmisor;
    @FXML private TableColumn<Mensaje, String> colMiMsjAsunto;
    @FXML private TableColumn<Mensaje, String> colMiMsjFecha;
    @FXML private TableColumn<Mensaje, String> colMiMsjLeido;
    @FXML private TextArea txtMensajeContenido;
    @FXML private TextField txtIdSolicitud;

    // Servicios
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private MensajeService mensajeService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private StageManager stageManager;
    @Autowired private SolicitudVacacionService solicitudVacacionService;

    // Data
    private Usuario usuarioActual;
    private ObservableList<Asistencia> asistencias = FXCollections.observableArrayList();
    private ObservableList<Mensaje> mensajes = FXCollections.observableArrayList();
    private ObservableList<SolicitudVacacion> misVacaciones = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        usuarioActual = SessionManager.getInstance().getUsuarioActual();

        mostrarDatosUsuario();
        mostrarHorarioAsignado();
        configurarTablaAsistencias();
        configurarTablaMensajes();
        configurarTablaVacacionesEmpleado();

        cargarAsistencias();
        cargarMensajes();
        cargarMisVacaciones();
        actualizarEstadoAsistenciaHoy();
        actualizarEstadisticas();
    }

    private void mostrarDatosUsuario() {
        lblNombre.setText(usuarioActual.getNombre());
        lblUsername.setText("Usuario: " + usuarioActual.getUsername());

        if (lblRol != null) {
            lblRol.setText("Rol: " + usuarioActual.getRol());
        }
        if (lblUsuario != null) {
            lblUsuario.setText("Empleado: " + usuarioActual.getNombre());
        }
        if (lblFechaHoy != null) {
            lblFechaHoy.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (lblHoraActual != null) {
            lblHoraActual.setText("Hora: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    private void mostrarHorarioAsignado() {
        if (lblHorarioInfo != null && usuarioActual.getHorario() != null) {
            lblHorarioInfo.setText(String.format("Tu horario: %s (%s - %s) | Tolerancia: %d min",
                    usuarioActual.getHorario().getNombre(),
                    usuarioActual.getHorario().getHoraEntrada().format(DateTimeFormatter.ofPattern("HH:mm")),
                    usuarioActual.getHorario().getHoraSalida().format(DateTimeFormatter.ofPattern("HH:mm")),
                    usuarioActual.getHorario().getToleranciaMinutos()
            ));
        }
    }

    private void actualizarEstadoAsistenciaHoy() {
        Asistencia asistenciaHoy = asistenciaService.obtenerAsistenciaHoy(usuarioActual);

        if (asistenciaHoy == null) {
            if (lblEstadoAsistenciaHoy != null) {
                lblEstadoAsistenciaHoy.setText("Estado Hoy: Sin registrar");
                lblEstadoAsistenciaHoy.setStyle("-fx-text-fill: #718096;");
            }
            btnMarcarAsistencia.setDisable(false);
            btnRegistrarSalida.setDisable(true);
        } else {
            String mensajeEstado = asistenciaService.obtenerMensajeEstado(asistenciaHoy);

            if (lblEstadoAsistenciaHoy != null) {
                lblEstadoAsistenciaHoy.setText("Estado Hoy: " + mensajeEstado);

                switch (asistenciaHoy.getEstado()) {
                    case "PRESENTE":
                        lblEstadoAsistenciaHoy.setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                        break;
                    case "TARDE":
                        lblEstadoAsistenciaHoy.setStyle("-fx-text-fill: #ed8936; -fx-font-weight: bold;");
                        break;
                    case "AUSENTE":
                        lblEstadoAsistenciaHoy.setStyle("-fx-text-fill: #f56565; -fx-font-weight: bold;");
                        break;
                    case "JUSTIFICADO":
                        lblEstadoAsistenciaHoy.setStyle("-fx-text-fill: #4299e1; -fx-font-weight: bold;");
                        break;
                }
            }

            btnMarcarAsistencia.setDisable(true);
            btnRegistrarSalida.setDisable(asistenciaHoy.getHoraSalida() != null);
        }
    }

    private void configurarTablaAsistencias() {
        if (tableMisAsistencias != null && colMiAsistFecha != null) {
            colMiAsistFecha.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
            );

            colMiAsistEntrada.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getHoraEntrada().format(DateTimeFormatter.ofPattern("HH:mm"))
                    )
            );

            colMiAsistSalida.setCellValueFactory(cellData -> {
                String horaSalida = cellData.getValue().getHoraSalida() != null
                        ? cellData.getValue().getHoraSalida().format(DateTimeFormatter.ofPattern("HH:mm"))
                        : "---";
                return new javafx.beans.property.SimpleStringProperty(horaSalida);
            });

            colMiAsistEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

            // Aplicar colores en celdas de estado
            colMiAsistEstado.setCellFactory(column -> new TableCell<Asistencia, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        switch (item) {
                            case "PRESENTE":
                                setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                                break;
                            case "TARDE":
                                setStyle("-fx-text-fill: #ed8936; -fx-font-weight: bold;");
                                break;
                            case "AUSENTE":
                                setStyle("-fx-text-fill: #f56565; -fx-font-weight: bold;");
                                break;
                            case "JUSTIFICADO":
                                setStyle("-fx-text-fill: #4299e1; -fx-font-weight: bold;");
                                break;
                        }
                    }
                }
            });

            tableMisAsistencias.setItems(asistencias);
        }
    }

    private void configurarTablaMensajes() {
        if (tableMisMensajes != null && colMiMsjEmisor != null) {
            colMiMsjEmisor.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getEmisor().getNombre()
                    )
            );
            colMiMsjAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
            colMiMsjFecha.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getFechaEnvio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    )
            );
            colMiMsjLeido.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getLeido() ? "✓" : "●"
                    )
            );

            tableMisMensajes.setItems(mensajes);

            tableMisMensajes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && txtMensajeContenido != null) {
                    txtMensajeContenido.setText(newVal.getContenido());
                    if (!newVal.getLeido()) {
                        mensajeService.marcarComoLeido(newVal.getId());
                        cargarMensajes();
                    }
                }
            });
        }
    }

    private void configurarTablaVacacionesEmpleado() {
        if (tableMisVacaciones != null && colVacFechaInicio != null) {
            colVacId.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ((SolicitudVacacion) cellData.getValue()).getId().toString())
            );

            colVacFechaInicio.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ((SolicitudVacacion) cellData.getValue()).getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
            );
            colVacFechaFin.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ((SolicitudVacacion) cellData.getValue()).getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
            );
            colVacDias.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(((SolicitudVacacion) cellData.getValue()).getDiasSolicitados())
                    )
            );
            colVacEstado.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ((SolicitudVacacion) cellData.getValue()).getEstado().toString()
                    )
            );
            colVacFechaSolicitud.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            ((SolicitudVacacion) cellData.getValue()).getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    )
            );

            tableMisVacaciones.setItems((ObservableList) misVacaciones);
        }
    }

    private void cargarAsistencias() {
        asistencias.clear();
        asistencias.addAll(asistenciaService.listarPorUsuario(usuarioActual));
    }

    private void cargarMensajes() {
        mensajes.clear();
        mensajes.addAll(mensajeService.obtenerMensajesRecibidos(usuarioActual));

        long noLeidos = mensajes.stream().filter(m -> !m.getLeido()).count();
        if (lblMensajesNoLeidos != null) {
            lblMensajesNoLeidos.setText(noLeidos > 0 ? "(" + noLeidos + " nuevos)" : "");
        }
    }

    private void cargarMisVacaciones() {
        misVacaciones.clear();
        misVacaciones.addAll(solicitudVacacionService.listarPorEmpleado(usuarioActual));
    }

    private void actualizarEstadisticas() {
        long presente = asistencias.stream().filter(a -> a.getEstado().equals("PRESENTE")).count();
        long tarde = asistencias.stream().filter(a -> a.getEstado().equals("TARDE")).count();
        long ausente = asistencias.stream().filter(a -> a.getEstado().equals("AUSENTE")).count();
        long justificado = asistencias.stream().filter(a -> a.getEstado().equals("JUSTIFICADO")).count();

        if (lblTotalPresente != null) lblTotalPresente.setText("✓ Presente: " + presente);
        if (lblTotalTarde != null) lblTotalTarde.setText("⚠ Tarde: " + tarde);
        if (lblTotalAusente != null) lblTotalAusente.setText("✗ Ausente: " + ausente);
        if (lblTotalJustificado != null) lblTotalJustificado.setText("ℹ Justificado: " + justificado);
    }

    @FXML
    private void handleMarcarAsistencia() {
        try {
            Asistencia asistencia = asistenciaService.registrarAsistencia(usuarioActual);

            String mensaje;
            Alert.AlertType tipo;

            if (asistencia.getEstado().equals("PRESENTE")) {
                mensaje = "¡Excelente! Asistencia registrada correctamente.\n\n✓ Llegaste a tiempo";
                tipo = Alert.AlertType.INFORMATION;
            } else {
                mensaje = "Asistencia registrada.\n\n⚠ Has llegado tarde según tu horario asignado.";
                tipo = Alert.AlertType.WARNING;
            }

            cargarAsistencias();
            actualizarEstadoAsistenciaHoy();
            actualizarEstadisticas();
            mostrarAlerta("Entrada Registrada", mensaje, tipo);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegistrarSalida() {
        try {
            Asistencia asistenciaHoy = asistenciaService.obtenerAsistenciaHoy(usuarioActual);

            if (asistenciaHoy == null) {
                throw new RuntimeException("No hay registro de entrada para hoy");
            }

            asistenciaService.registrarSalida(asistenciaHoy.getId());

            cargarAsistencias();
            actualizarEstadoAsistenciaHoy();
            mostrarAlerta("Salida Registrada", "Tu hora de salida ha sido registrada correctamente.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarMisAsistencias() {
        cargarAsistencias();
        actualizarEstadisticas();
    }

    @FXML
    private void handleNuevoMensaje() {
        Dialog<Mensaje> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Mensaje");
        dialog.setHeaderText("Enviar mensaje");

        ButtonType btnEnviar = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEnviar, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Usuario> cmbReceptor = new ComboBox<>();
        cmbReceptor.getItems().addAll(
                usuarioService.listarTodos().stream()
                        .filter(u -> u.getRol() == Rol.ADMIN ||
                                (u.getRol() == Rol.EMPLEADO && !u.getId().equals(usuarioActual.getId())))
                        .toList()
        );

        TextField txtAsunto = new TextField();
        TextArea txtContenido = new TextArea();
        txtContenido.setPrefRowCount(4);

        grid.add(new Label("Para:"), 0, 0);
        grid.add(cmbReceptor, 1, 0);
        grid.add(new Label("Asunto:"), 0, 1);
        grid.add(txtAsunto, 1, 1);
        grid.add(new Label("Mensaje:"), 0, 2);
        grid.add(txtContenido, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnEnviar) {
                if (cmbReceptor.getValue() == null || txtAsunto.getText().trim().isEmpty()) {
                    return null;
                }
                return mensajeService.enviarMensaje(
                        usuarioActual,
                        cmbReceptor.getValue(),
                        txtAsunto.getText(),
                        txtContenido.getText()
                );
            }
            return null;
        });

        Optional<Mensaje> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            mostrarAlerta("Éxito", "Mensaje enviado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleSolicitarVacaciones() {
        Dialog<SolicitudVacacion> dialog = new Dialog<>();
        dialog.setTitle("Solicitar Vacaciones");
        dialog.setHeaderText("Nueva Solicitud de Vacaciones");

        ButtonType btnSolicitar = new ButtonType("Solicitar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSolicitar, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        DatePicker dpFechaInicio = new DatePicker();
        DatePicker dpFechaFin = new DatePicker();
        TextArea txtMotivo = new TextArea();
        txtMotivo.setPrefRowCount(3);

        grid.add(new Label("Fecha Inicio:"), 0, 0);
        grid.add(dpFechaInicio, 1, 0);
        grid.add(new Label("Fecha Fin:"), 0, 1);
        grid.add(dpFechaFin, 1, 1);
        grid.add(new Label("Motivo:"), 0, 2);
        grid.add(txtMotivo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSolicitar) {
                if (dpFechaInicio.getValue() == null || dpFechaFin.getValue() == null || txtMotivo.getText().trim().isEmpty()) {
                    return null;
                }
                try {
                    return solicitudVacacionService.solicitarVacaciones(
                            usuarioActual,
                            dpFechaInicio.getValue(),
                            dpFechaFin.getValue(),
                            txtMotivo.getText()
                    );
                } catch (Exception e) {
                    mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<SolicitudVacacion> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            cargarMisVacaciones();
            mostrarAlerta("Éxito", "Solicitud enviada correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleActualizarVacaciones() {
        cargarMisVacaciones();
        mostrarAlerta("Actualizado", "Lista de vacaciones actualizada", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleActualizarMensajes() {
        cargarMensajes();
    }

    @FXML
    private void handleMarcarTodosLeidos() {
        mensajeService.marcarTodosComoLeidos(usuarioActual);
        cargarMensajes();
        mostrarAlerta("Éxito", "Todos los mensajes marcados como leídos", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
        stageManager.cambiarEscena("/fxml/login.fxml", "Sistema de Asistencia - Login", 400, 500);
    }

    @FXML
    private void handleGenerarSolicitudVacaciones(){
        try {
            String idSolicitudText = txtIdSolicitud.getText();

            // VALIDACIÓN 1: Campo vacío
            if (idSolicitudText == null || idSolicitudText.trim().isEmpty()) {
                mostrarAlerta("Error", "El campo ID de solicitud no puede estar vacío", Alert.AlertType.WARNING);
                return;
            }

            // VALIDACIÓN 2: Formato numérico
            long idSolicitud;
            try {
                idSolicitud = Long.parseLong(idSolicitudText.trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El ID de solicitud debe ser un número válido", Alert.AlertType.WARNING);
                return;
            }

            // VALIDACIÓN 3: Rango positivo
            if (idSolicitud <= 0) {
                mostrarAlerta("Error", "El ID de solicitud debe ser un número positivo", Alert.AlertType.WARNING);
                return;
            }

            // VALIDACIÓN 4: Verificar que la solicitud existe (USANDO buscarPorId)
            Optional<SolicitudVacacion> solicitud = solicitudVacacionService.buscarPorId(idSolicitud);
            if (solicitud.isEmpty()) {
                mostrarAlerta("Error", "No se encontró una solicitud con el ID: " + idSolicitud, Alert.AlertType.WARNING);
                return;
            }

            // VALIDACIÓN 5: Verificar que la solicitud pertenece al usuario actual
            if (!solicitud.get().getEmpleado().getId().equals(usuarioActual.getId())) {
                mostrarAlerta("Error", "La solicitud con ID " + idSolicitud + " no pertenece a tu usuario", Alert.AlertType.WARNING);
                return;
            }

            // Generar el reporte si pasó todas las validaciones
            JasperPrint jasperPrint = solicitudVacacionService.runReportSolicitud(idSolicitud);

            // Guardar como PDF
            String outputFile = System.getProperty("user.home") + "/Desktop/Solicitud_Vacaciones_" + idSolicitud + ".pdf";

            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile);

            // Abrir el archivo PDF
            openPDFFile(outputFile);

            mostrarAlerta("Éxito", "Reporte generado en: " + outputFile, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el reporte: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openPDFFile(String filePath) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(new java.io.File(filePath));
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo abrir el PDF automáticamente: " + e.getMessage());
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