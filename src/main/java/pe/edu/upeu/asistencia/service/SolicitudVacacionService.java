package pe.edu.upeu.asistencia.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.edu.upeu.asistencia.model.SolicitudVacacion;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.enums.EstadoSolicitud;
import pe.edu.upeu.asistencia.repository.SolicitudVacacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudVacacionService {

    private final SolicitudVacacionRepository solicitudRepository;

    @Autowired
    private DataSource dataSource;

    public SolicitudVacacionService(SolicitudVacacionRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public SolicitudVacacion solicitarVacaciones(Usuario empleado, LocalDate fechaInicio,
                                                 LocalDate fechaFin, String motivo) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha fin");
        }

        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new RuntimeException("No puede solicitar vacaciones en fechas pasadas");
        }

        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;

        if (dias > 30) {
            throw new RuntimeException("No puede solicitar más de 30 días de vacaciones");
        }

        SolicitudVacacion solicitud = new SolicitudVacacion();
        solicitud.setEmpleado(empleado);
        solicitud.setFechaInicio(fechaInicio);
        solicitud.setFechaFin(fechaFin);
        solicitud.setMotivo(motivo);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setDiasSolicitados((int) dias);

        return solicitudRepository.save(solicitud);
    }

    public SolicitudVacacion aprobar(Long solicitudId, Usuario admin, String comentario) {
        SolicitudVacacion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Esta solicitud ya fue procesada");
        }

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setAprobadoPor(admin);
        solicitud.setComentarioAdmin(comentario);

        return solicitudRepository.save(solicitud);
    }

    public SolicitudVacacion rechazar(Long solicitudId, Usuario admin, String comentario) {
        SolicitudVacacion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Esta solicitud ya fue procesada");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setAprobadoPor(admin);
        solicitud.setComentarioAdmin(comentario);

        return solicitudRepository.save(solicitud);
    }

    public List<SolicitudVacacion> listarPorEmpleado(Usuario empleado) {
        return solicitudRepository.findByEmpleadoOrderByFechaSolicitudDesc(empleado);
    }

    public List<SolicitudVacacion> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    public Optional<SolicitudVacacion> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public SolicitudVacacion obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
    }

    // Método para obtener archivos desde resources
    private File getFile(String filename) throws IOException {
        Resource resource = new ClassPathResource(filename);
        return resource.getFile();
    }

    public JasperPrint runReport() throws JRException, IOException {
        HashMap<String, Object> param = new HashMap<>();

        // Obtener ruta de la imagen desde resources
        String imagen = getFile("img/logo.png").getAbsolutePath();
        param.put("imagenurl", imagen);

        // Cargar el diseño del informe
        JasperDesign jdesign = JRXmlLoader.load(getFile("jasper/report_vac.jrxml"));
        JasperReport jreport = JasperCompileManager.compileReport(jdesign);

        Connection conn = null;
        try {
            // Obtener conexión directamente del DataSource
            conn = dataSource.getConnection();

            // Configurar SQLite para mejor rendimiento
            try (var stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA cache_size=10000");
            }

            // Llenar el reporte
            return JasperFillManager.fillReport(jreport, param, conn);

        } catch (SQLException e) {
            throw new JRException("Error de base de datos al generar reporte", e);
        } finally {
            // Cerrar conexión explícitamente
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
    public JasperPrint runReportSolicitud(Long id) throws JRException, IOException {
        HashMap<String, Object> param = new HashMap<>();

        // Obtener ruta de la imagen desde resources
        String imagen = getFile("img/logo.png").getAbsolutePath();
        param.put("imagenurl", imagen);
        param.put("idSolicitud", id);

        // Cargar el diseño del informe
        JasperDesign jdesign = JRXmlLoader.load(getFile("jasper/solitud_vac.jrxml"));
        JasperReport jreport = JasperCompileManager.compileReport(jdesign);

        Connection conn = null;
        try {
            // Obtener conexión directamente del DataSource
            conn = dataSource.getConnection();

            // Configurar SQLite para mejor rendimiento
            try (var stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA cache_size=10000");
            }

            // Llenar el reporte
            return JasperFillManager.fillReport(jreport, param, conn);

        } catch (SQLException e) {
            throw new JRException("Error de base de datos al generar reporte", e);
        } finally {
            // Cerrar conexión explícitamente
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
}