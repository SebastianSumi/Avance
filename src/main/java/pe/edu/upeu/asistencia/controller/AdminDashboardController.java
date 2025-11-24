package pe.edu.upeu.asistencia.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import pe.edu.upeu.asistencia.dto.ResumenAsistenciaDTO;
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
import pe.edu.upeu.asistencia.enums.EstadoSolicitud;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AdminDashboardController {

    // CAMPOS DE BÚSQUEDA
    @FXML private TextField txtBuscarUsuario;
    @FXML private TextField txtBuscarDashboard;
    @FXML private TextField txtBuscarAsistencia;

    // USUARIOS
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private Button btnNuevoUsuario;
    @FXML private Button btnEditarUsuario;
    @FXML private Button btnEliminarUsuario;

    // ASISTENCIAS
    @FXML private TableView<Asistencia> tblAsistencias;
    @FXML private TableColumn<Asistencia, Long> colAsistenciaId;
    @FXML private TableColumn<Asistencia, String> colEmpleado;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, String> colHoraEntrada;
    @FXML private TableColumn<Asistencia, String> colHoraSalida;
    @FXML private TableColumn<Asistencia, String> colEstado;
    @FXML private Button btnRegistrarAsistencia;
    @FXML private Button btnEliminarAsistencia;

    // MENSAJES
    @FXML private TableView<Mensaje> tblMensajes;
    @FXML private TableColumn<Mensaje, String> colMensajeEmisor;
    @FXML private TableColumn<Mensaje, String> colMensajeReceptor;
    @FXML private TableColumn<Mensaje, String> colAsunto;
    @FXML private TableColumn<Mensaje, String> colFechaEnvio;
    @FXML private Button btnNuevoMensaje;
    @FXML private Button btnEliminarMensaje;
    @FXML private TextArea txtContenidoMensaje;

    // VACACIONES
    @FXML private TableView<SolicitudVacacion> tblSolicitudesVacacion;
    @FXML private TableColumn<SolicitudVacacion, String> colVacEmpleado;
    @FXML private TableColumn<SolicitudVacacion, String> colVacFechaInicio;
    @FXML private TableColumn<SolicitudVacacion, String> colVacFechaFin;
    @FXML private TableColumn<SolicitudVacacion, Integer> colVacDias;
    @FXML private TableColumn<SolicitudVacacion, String> colVacMotivo;
    @FXML private TableColumn<SolicitudVacacion, String> colVacEstado;
    @FXML private TableColumn<SolicitudVacacion, String> colVacFechaSolicitud;
    @FXML private Button btnAprobarVacacion;
    @FXML private Button btnRechazarVacacion;

    // DASHBOARD
    @FXML private ComboBox<String> cboPeriodoDashboard;
    @FXML private TableView<ResumenAsistenciaDTO> tableDashboardAsistencias;
    @FXML private TableColumn<ResumenAsistenciaDTO, String> colDashEmpleado;
    @FXML private TableColumn<ResumenAsistenciaDTO, Long> colDashPresente;
    @FXML private TableColumn<ResumenAsistenciaDTO, Long> colDashTarde;
    @FXML private TableColumn<ResumenAsistenciaDTO, Long> colDashAusente;
    @FXML private TableColumn<ResumenAsistenciaDTO, Long> colDashJustificado;
    @FXML private TableColumn<ResumenAsistenciaDTO, Long> colDashTotal;
    @FXML private TableColumn<ResumenAsistenciaDTO, String> colDashPorcentaje;
    @FXML private Label lblTotalEmpleados;
    @FXML private Label lblPromedioAsistencia;
    @FXML private Label lblTotalRegistros;
    @FXML private Button btnGenerarReporteAsistencias;

    @Autowired private SolicitudVacacionService solicitudVacacionService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private MensajeService mensajeService;
    @Autowired private ConfigurableApplicationContext springContext;
    @Autowired private StageManager stageManager;

    private ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();

    private ObservableList<Asistencia> asistencias = FXCollections.observableArrayList();
    private ObservableList<Asistencia> asistenciasFiltradas = FXCollections.observableArrayList();

    private ObservableList<Mensaje> mensajes = FXCollections.observableArrayList();
    private ObservableList<SolicitudVacacion> solicitudesVacacion = FXCollections.observableArrayList();

    private ObservableList<ResumenAsistenciaDTO> dashboardData = FXCollections.observableArrayList();
    private ObservableList<ResumenAsistenciaDTO> dashboardFiltrado = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTablaDashboard();
        configurarTablaUsuarios();
        configurarTablaAsistencias();
        configurarTablaMensajes();
        configurarTablaVacaciones();

        cargarUsuarios();
        cargarAsistencias();
        cargarMensajes();
        cargarSolicitudesVacaciones();
        cargarDashboard();

        // Configurar búsqueda en tiempo real
        configurarBusquedas();
    }


    private void configurarBusquedas() {
        // Búsqueda de usuarios
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((observable, oldValue, newValue) -> {
                buscarUsuarios(newValue);
            });
        }

        // Búsqueda en dashboard
        if (txtBuscarDashboard != null) {
            txtBuscarDashboard.textProperty().addListener((observable, oldValue, newValue) -> {
                buscarEnDashboard(newValue);
            });
        }

        // Búsqueda en asistencias
        if (txtBuscarAsistencia != null) {
            txtBuscarAsistencia.textProperty().addListener((observable, oldValue, newValue) -> {
                buscarAsistencias(newValue);
            });
        }
    }

    private void buscarUsuarios(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            tblUsuarios.setItems(usuarios);
        } else {
            String busqueda = textoBusqueda.toLowerCase().trim();
            usuariosFiltrados.clear();
            usuariosFiltrados.addAll(
                    usuarios.stream()
                            .filter(u -> u.getNombre().toLowerCase().contains(busqueda) ||
                                    u.getUsername().toLowerCase().contains(busqueda))
                            .collect(Collectors.toList())
            );
            tblUsuarios.setItems(usuariosFiltrados);
        }
    }

    private void buscarEnDashboard(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            tableDashboardAsistencias.setItems(dashboardData);
            actualizarEstadisticasGenerales(dashboardData);
        } else {
            String busqueda = textoBusqueda.toLowerCase().trim();
            dashboardFiltrado.clear();
            dashboardFiltrado.addAll(
                    dashboardData.stream()
                            .filter(dto -> dto.getNombreEmpleado().toLowerCase().contains(busqueda))
                            .collect(Collectors.toList())
            );
            tableDashboardAsistencias.setItems(dashboardFiltrado);
            actualizarEstadisticasGenerales(dashboardFiltrado);
        }
    }

    private void buscarAsistencias(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            tblAsistencias.setItems(asistencias);
        } else {
            String busqueda = textoBusqueda.toLowerCase().trim();
            asistenciasFiltradas.clear();
            asistenciasFiltradas.addAll(
                    asistencias.stream()
                            .filter(a -> a.getUsuario().getNombre().toLowerCase().contains(busqueda))
                            .collect(Collectors.toList())
            );
            tblAsistencias.setItems(asistenciasFiltradas);
        }
    }


    private void configurarTablaUsuarios() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        tblUsuarios.setItems(usuarios);
    }

    private void configurarTablaDashboard() {
        colDashEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        colDashPresente.setCellValueFactory(new PropertyValueFactory<>("presente"));
        colDashTarde.setCellValueFactory(new PropertyValueFactory<>("tarde"));
        colDashAusente.setCellValueFactory(new PropertyValueFactory<>("ausente"));
        colDashJustificado.setCellValueFactory(new PropertyValueFactory<>("justificado"));
        colDashTotal.setCellValueFactory(new PropertyValueFactory<>("totalGeneral"));
        colDashPorcentaje.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPorcentajeFormateado())
        );

        colDashPresente.setStyle("-fx-alignment: CENTER;");
        colDashTarde.setStyle("-fx-alignment: CENTER;");
        colDashAusente.setStyle("-fx-alignment: CENTER;");
        colDashJustificado.setStyle("-fx-alignment: CENTER;");
        colDashTotal.setStyle("-fx-alignment: CENTER;");
        colDashPorcentaje.setStyle("-fx-alignment: CENTER;");

        cboPeriodoDashboard.setItems(FXCollections.observableArrayList(
                "Todo el tiempo", "Última semana", "Último mes", "Últimos 3 meses"
        ));
        cboPeriodoDashboard.setValue("Todo el tiempo");
    }

    private void cargarDashboard() {
        List<ResumenAsistenciaDTO> lista = new ArrayList<>();
        String periodoSeleccionado = cboPeriodoDashboard.getValue();
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = null;

        switch (periodoSeleccionado) {
            case "Última semana":
                fechaInicio = hoy.minusWeeks(1);
                break;
            case "Último mes":
                fechaInicio = hoy.minusMonths(1);
                break;
            case "Últimos 3 meses":
                fechaInicio = hoy.minusMonths(3);
                break;
        }

        if (fechaInicio != null) {
            lista = asistenciaService.generarResumenPorPeriodo(fechaInicio, hoy);
        } else {
            lista = asistenciaService.generarResumenPorEmpleado();
        }

        lista.forEach(dto -> {
            long total = dto.getPresente() + dto.getTarde() + dto.getAusente() + dto.getJustificado();
            dto.setTotalGeneral(total);
            if (total > 0) {
                dto.setPorcentajeAsistencia((dto.getPresente() + dto.getJustificado()) * 100.0 / total);
            } else {
                dto.setPorcentajeAsistencia(0.0);
            }
        });

        dashboardData.clear();
        dashboardData.addAll(lista);
        tableDashboardAsistencias.setItems(dashboardData);
        actualizarEstadisticasGenerales(dashboardData);
    }

    @FXML
    private void handleFiltrarDashboard() {
        cargarDashboard();
    }

    @FXML
    public void handleActualizarDashboard() {
        cargarDashboard();
        mostrarInfo("Dashboard actualizado");
    }

    private void actualizarEstadisticasGenerales(ObservableList<ResumenAsistenciaDTO> resumen) {
        int totalEmpleados = resumen.size();
        lblTotalEmpleados.setText(String.valueOf(totalEmpleados));

        long totalRegistros = resumen.stream()
                .mapToLong(ResumenAsistenciaDTO::getTotalGeneral)
                .sum();
        lblTotalRegistros.setText(String.valueOf(totalRegistros));

        if (totalEmpleados > 0) {
            double promedioAsistencia = resumen.stream()
                    .mapToDouble(ResumenAsistenciaDTO::getPorcentajeAsistencia)
                    .average()
                    .orElse(0.0);
            lblPromedioAsistencia.setText(String.format("%.1f%%", promedioAsistencia));
        } else {
            lblPromedioAsistencia.setText("0%");
        }
    }

    private void configurarTablaAsistencias() {
        colAsistenciaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmpleado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsuario().getNombre())
        );
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
        );
        colHoraEntrada.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getHoraEntrada().format(DateTimeFormatter.ofPattern("HH:mm"))
                )
        );
        colHoraSalida.setCellValueFactory(cellData -> {
            String horaSalida = cellData.getValue().getHoraSalida() != null
                    ? cellData.getValue().getHoraSalida().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : " ";
            return new SimpleStringProperty(horaSalida);
        });
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tblAsistencias.setItems(asistencias);
    }

    private void configurarTablaMensajes() {
        colMensajeEmisor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmisor().getNombre())
        );
        colMensajeReceptor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReceptor().getNombre())
        );
        colAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colFechaEnvio.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFechaEnvio().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        )
                )
        );

        tblMensajes.setItems(mensajes);
        tblMensajes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtContenidoMensaje.setText(newVal.getContenido());
            }
        });
    }

    private void configurarTablaVacaciones() {
        if (colVacEmpleado != null) {
            colVacEmpleado.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmpleado().getNombre())
            );
            colVacFechaInicio.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
            );
            colVacFechaFin.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
            );
            colVacDias.setCellValueFactory(new PropertyValueFactory<>("diasSolicitados"));
            colVacMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
            colVacEstado.setCellValueFactory(cellData -> {
                String estado = cellData.getValue().getEstado().toString();
                return new SimpleStringProperty(estado);
            });
            colVacFechaSolicitud.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    )
            );
            tblSolicitudesVacacion.setItems(solicitudesVacacion);
        }
    }


    private void cargarUsuarios() {
        usuarios.clear();
        usuarios.addAll(usuarioService.listarTodos());
    }

    private void cargarAsistencias() {
        asistencias.clear();
        asistencias.addAll(asistenciaService.listarTodas());
    }

    private void cargarMensajes() {
        mensajes.clear();
        Usuario admin = SessionManager.getInstance().getUsuarioActual();
        mensajes.addAll(mensajeService.obtenerMensajesRecibidos(admin));
    }

    private void cargarSolicitudesVacaciones() {
        solicitudesVacacion.clear();
        solicitudesVacacion.addAll(solicitudVacacionService.listarTodas());
    }


    @FXML
    private void handleNuevoUsuario() {
        abrirFormularioUsuario(null);
    }

    @FXML
    private void handleEditarUsuario() {
        Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione un usuario", Alert.AlertType.WARNING);
            return;
        }
        abrirFormularioUsuario(seleccionado);
    }

    @FXML
    private void handleEliminarUsuario() {
        Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione un usuario", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar usuario?");
        confirmacion.setContentText("¿Está seguro de eliminar a " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                usuarioService.eliminarUsuario(seleccionado.getId());
                cargarUsuarios();
                mostrarAlerta("Éxito", "Usuario eliminado correctamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el usuario: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void abrirFormularioUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/usuario_form.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new Scene(loader.load(), 400, 500));
            stage.initModality(Modality.APPLICATION_MODAL);

            UsuarioFormController controller = loader.getController();
            controller.setUsuario(usuario);
            controller.setOnGuardar(() -> {
                cargarUsuarios();
                cargarDashboard();
                stage.close();
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el formulario", Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void handleGenerarReporteAsistencia(){

        try {
            JasperPrint jasperPrint = asistenciaService.runReport();

            // Guardar como PDF
            String outputFile = System.getProperty("user.home") + "/Desktop/Reporte_Asistencia.pdf";

            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile);

            // Abrir el archivo PDF
            openPDFFile(outputFile);

            mostrarAlerta("Éxito", "Reporte generado en: " + outputFile, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el reporte: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleRegistrarAsistencia() {
        ChoiceDialog<Usuario> dialog = new ChoiceDialog<>(null,
                usuarioService.listarTodos().stream()
                        .filter(u -> u.getRol().toString().equals("EMPLEADO"))
                        .toList()
        );
        dialog.setTitle("Registrar Asistencia");
        dialog.setHeaderText("Seleccione el empleado");
        dialog.setContentText("Empleado:");

        Optional<Usuario> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            try {
                asistenciaService.registrarAsistencia(resultado.get());
                cargarAsistencias();
                cargarDashboard();
                mostrarAlerta("Éxito", "Asistencia registrada correctamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleEliminarAsistencia() {
        Asistencia seleccionada = tblAsistencias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione una asistencia", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setContentText("¿Quiere eliminar asistencia?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            asistenciaService.eliminarAsistencia(seleccionada.getId());
            cargarAsistencias();
            cargarDashboard();
            mostrarAlerta("Hecho", "Asistencia eliminada", Alert.AlertType.INFORMATION);
        }
    }


    @FXML
    private void handleNuevoMensaje() {
        Dialog<Mensaje> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Mensaje");
        dialog.setHeaderText("Enviar mensaje a empleado");

        ButtonType btnEnviar = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEnviar, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Usuario> cmbReceptor = new ComboBox<>();
        cmbReceptor.getItems().addAll(
                usuarioService.listarTodos().stream()
                        .filter(u -> u.getRol().toString().equals("EMPLEADO"))
                        .toList()
        );
        TextField txtAsunto = new TextField();
        TextArea txtContenido = new TextArea();
        txtContenido.setPrefRowCount(4);

        grid.add(new Label("Receptor:"), 0, 0);
        grid.add(cmbReceptor, 1, 0);
        grid.add(new Label("Asunto:"), 0, 1);
        grid.add(txtAsunto, 1, 1);
        grid.add(new Label("Contenido:"), 0, 2);
        grid.add(txtContenido, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnEnviar) {
                if (cmbReceptor.getValue() != null && !txtAsunto.getText().isEmpty()) {
                    Usuario emisor = SessionManager.getInstance().getUsuarioActual();
                    return mensajeService.enviarMensaje(
                            emisor,
                            cmbReceptor.getValue(),
                            txtAsunto.getText(),
                            txtContenido.getText()
                    );
                }
            }
            return null;
        });

        Optional<Mensaje> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            mostrarAlerta("Éxito", "Mensaje enviado correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleEliminarMensaje() {
        Mensaje seleccionado = tblMensajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione el mensaje", "Por favor seleccione un mensaje", Alert.AlertType.WARNING);
            return;
        }

        mensajeService.eliminar(seleccionado.getId());
        cargarMensajes();
        txtContenidoMensaje.clear();
        mostrarAlerta("Éxito", "Mensaje eliminado", Alert.AlertType.INFORMATION);
    }


    @FXML
    private void handleAprobarVacacion() {
        SolicitudVacacion seleccionada = tblSolicitudesVacacion.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione una solicitud", Alert.AlertType.WARNING);
            return;
        }

        if (seleccionada.getEstado() != EstadoSolicitud.PENDIENTE) {
            mostrarAlerta("Error", "Esta solicitud ya fue procesada", Alert.AlertType.ERROR);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Aprobar Solicitud");
        dialog.setHeaderText("Aprobar solicitud de vacaciones de " + seleccionada.getEmpleado().getNombre());
        dialog.setContentText("Comentario (opcional):");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(comentario -> {
            try {
                Usuario admin = SessionManager.getInstance().getUsuarioActual();
                solicitudVacacionService.aprobar(seleccionada.getId(), admin, comentario);
                cargarSolicitudesVacaciones();
                mostrarAlerta("Éxito", "Solicitud aprobada correctamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleRechazarVacacion() {
        SolicitudVacacion seleccionada = tblSolicitudesVacacion.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione una solicitud", Alert.AlertType.WARNING);
            return;
        }

        if (seleccionada.getEstado() != EstadoSolicitud.PENDIENTE) {
            mostrarAlerta("Error", "Esta solicitud ya fue procesada", Alert.AlertType.ERROR);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rechazar Solicitud");
        dialog.setHeaderText("Rechazar solicitud de vacaciones de " + seleccionada.getEmpleado().getNombre());
        dialog.setContentText("Motivo del rechazo:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                mostrarAlerta("Error", "Debe indicar el motivo del rechazo", Alert.AlertType.ERROR);
                return;
            }
            try {
                Usuario admin = SessionManager.getInstance().getUsuarioActual();
                solicitudVacacionService.rechazar(seleccionada.getId(), admin, motivo);
                cargarSolicitudesVacaciones();
                mostrarAlerta("Éxito", "Solicitud rechazada correctamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleVerDetalleVacacion() {
        SolicitudVacacion seleccionada = tblSolicitudesVacacion.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione una solicitud", Alert.AlertType.WARNING);
            return;
        }

        StringBuilder detalle = new StringBuilder();
        detalle.append("SOLICITUD DE VACACIONES\n\n");
        detalle.append("Empleado: ").append(seleccionada.getEmpleado().getNombre()).append("\n");
        detalle.append("Fecha Inicio: ").append(seleccionada.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        detalle.append("Fecha Fin: ").append(seleccionada.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        detalle.append("Días: ").append(seleccionada.getDiasSolicitados()).append("\n");
        detalle.append("Motivo: ").append(seleccionada.getMotivo()).append("\n");
        detalle.append("Estado: ").append(seleccionada.getEstado()).append("\n");
        detalle.append("Fecha Solicitud: ").append(seleccionada.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

        if (seleccionada.getFechaRespuesta() != null) {
            detalle.append("\nFecha Respuesta: ").append(seleccionada.getFechaRespuesta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            detalle.append("Respondido por: ").append(seleccionada.getAprobadoPor().getNombre()).append("\n");
            if (seleccionada.getComentarioAdmin() != null) {
                detalle.append("Comentario Admin: ").append(seleccionada.getComentarioAdmin()).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de Solicitud");
        alert.setHeaderText(null);
        alert.setContentText(detalle.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleActualizarVacaciones() {
        cargarSolicitudesVacaciones();
        mostrarAlerta("Éxito", "Lista actualizada", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
        stageManager.cambiarEscena("/fxml/login.fxml", "Sistema de Asistencia - Login", 400, 500);
    }

    @FXML
    private void handleGenerarReporteVacaciones() {
        try {
            JasperPrint jasperPrint = solicitudVacacionService.runReport();

            // Guardar como PDF
            String outputFile = System.getProperty("user.home") + "/Desktop/Reporte_Vacaciones.pdf";

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

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}