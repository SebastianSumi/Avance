package pe.edu.upeu.asistencia.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pe.edu.upeu.asistencia.dto.ResumenAsistenciaDTO;
import pe.edu.upeu.asistencia.enums.EstadoAsistencia;
import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.model.Asistencia;
import pe.edu.upeu.asistencia.model.Horario;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.repository.AsistenciaRepository;
import pe.edu.upeu.asistencia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class AsistenciaService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public Asistencia registrarAsistencia(Usuario usuario) {

        if (yaRegistroHoy(usuario)) {
            throw new RuntimeException("Ya existe un registro de asistencia para hoy");
        }

        // Verificar que el usuario tenga un horario asignado
        Horario horario = usuario.getHorario();
        if (horario == null) {
            throw new RuntimeException("El usuario no tiene un horario asignado");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(LocalDate.now());

        LocalTime horaActual = LocalTime.now();
        asistencia.setHoraEntrada(horaActual);

        // Calcular el estado según el horario
        String estado = calcularEstadoAsistencia(horaActual, horario);
        asistencia.setEstado(estado);

        System.out.println("REGISTRO DE ASISTENCIA");
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Hora actual: " + horaActual);
        System.out.println("Hora entrada esperada: " + horario.getHoraEntrada());
        System.out.println("Tolerancia: " + horario.getToleranciaMinutos() + " minutos");
        System.out.println("Estado asignado: " + estado);

        return asistenciaRepository.save(asistencia);
    }

    private String calcularEstadoAsistencia(LocalTime horaLlegada, Horario horario) {
        LocalTime horaEntradaEsperada = horario.getHoraEntrada();
        int toleranciaMinutos = horario.getToleranciaMinutos() != null ? horario.getToleranciaMinutos() : 15;

        // Calcular la diferencia en minutos
        long minutosRetraso = ChronoUnit.MINUTES.between(horaEntradaEsperada, horaLlegada);

        System.out.println("Minutos de diferencia: " + minutosRetraso);

        // Si llegó antes o dentro del periodo de tolerancia
        if (minutosRetraso <= toleranciaMinutos) {
            return EstadoAsistencia.PRESENTE.name();
        }
        // Si llegó después del periodo de tolerancia
        else {
            return EstadoAsistencia.TARDE.name();
        }
    }

    public Asistencia justificarAsistencia(Long asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        asistencia.setEstado(EstadoAsistencia.JUSTIFICADO.name());
        return asistenciaRepository.save(asistencia);
    }

    public Asistencia marcarAusente(Long asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        asistencia.setEstado(EstadoAsistencia.AUSENTE.name());
        return asistenciaRepository.save(asistencia);
    }


    public Asistencia registrarSalida(Long asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        if (asistencia.getHoraSalida() != null) {
            throw new RuntimeException("La salida ya fue registrada");
        }

        asistencia.setHoraSalida(LocalTime.now());
        return asistenciaRepository.save(asistencia);
    }


    public Asistencia obtenerAsistenciaHoy(Usuario usuario) {
        List<Asistencia> asistencias = asistenciaRepository.findByUsuarioAndFecha(usuario, LocalDate.now());
        return asistencias.isEmpty() ? null : asistencias.get(0);
    }

    // Listar todas las asistencias
    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }

    // Listar asistencias de un usuario
    public List<Asistencia> listarPorUsuario(Usuario usuario) {
        return asistenciaRepository.findByUsuario(usuario);
    }

    // Listar asistencias por fecha
    public List<Asistencia> listarPorFecha(LocalDate fecha) {
        return asistenciaRepository.findByFecha(fecha);
    }

    // Generar resumen de todos los empleados
    public List<ResumenAsistenciaDTO> generarResumenPorEmpleado() {
        List<ResumenAsistenciaDTO> resumen = new ArrayList<>();
        List<Usuario> empleados = usuarioRepository.findByRol(Rol.EMPLEADO);

        for (Usuario empleado : empleados) {
            resumen.add(generarResumenPorEmpleado(empleado));
        }

        return resumen;
    }

    // Generar resumen de un empleado
    public ResumenAsistenciaDTO generarResumenPorEmpleado(Usuario empleado) {
        long presente = asistenciaRepository.countByUsuarioAndEstado(empleado, EstadoAsistencia.PRESENTE.name());
        long tarde = asistenciaRepository.countByUsuarioAndEstado(empleado, EstadoAsistencia.TARDE.name());
        long ausente = asistenciaRepository.countByUsuarioAndEstado(empleado, EstadoAsistencia.AUSENTE.name());
        long justificado = asistenciaRepository.countByUsuarioAndEstado(empleado, EstadoAsistencia.JUSTIFICADO.name());

        return new ResumenAsistenciaDTO(
                empleado.getId(),
                empleado.getNombre(),
                presente,
                tarde,
                ausente,
                justificado
        );
    }

    // Generar resumen filtrado por periodo
    public List<ResumenAsistenciaDTO> generarResumenPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ResumenAsistenciaDTO> resumen = new ArrayList<>();
        List<Usuario> empleados = usuarioRepository.findByRol(Rol.EMPLEADO);

        for (Usuario empleado : empleados) {
            List<Asistencia> asistenciasPeriodo = asistenciaRepository.findByUsuarioAndFechaBetween(empleado, fechaInicio, fechaFin);

            long presente = asistenciasPeriodo.stream()
                    .filter(a -> a.getEstado().equals(EstadoAsistencia.PRESENTE.name()))
                    .count();
            long tarde = asistenciasPeriodo.stream()
                    .filter(a -> a.getEstado().equals(EstadoAsistencia.TARDE.name()))
                    .count();
            long ausente = asistenciasPeriodo.stream()
                    .filter(a -> a.getEstado().equals(EstadoAsistencia.AUSENTE.name()))
                    .count();
            long justificado = asistenciasPeriodo.stream()
                    .filter(a -> a.getEstado().equals(EstadoAsistencia.JUSTIFICADO.name()))
                    .count();

            resumen.add(new ResumenAsistenciaDTO(
                    empleado.getId(),
                    empleado.getNombre(),
                    presente,
                    tarde,
                    ausente,
                    justificado
            ));
        }

        return resumen;
    }

    // Buscar asistencia por ID
    public Asistencia buscarPorId(Long id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
    }

    // Eliminar asistencia
    public void eliminarAsistencia(Long id) {
        asistenciaRepository.deleteById(id);
    }

    // Verificar si ya registró asistencia hoy
    public boolean yaRegistroHoy(Usuario usuario) {
        return !asistenciaRepository.findByUsuarioAndFecha(usuario, LocalDate.now()).isEmpty();
    }
    
    public String obtenerMensajeEstado(Asistencia asistencia) {
        if (asistencia == null) return "Sin registro";

        switch (EstadoAsistencia.valueOf(asistencia.getEstado())) {
            case PRESENTE:
                return "Llegó a tiempo ✓";
            case TARDE:
                Usuario usuario = asistencia.getUsuario();
                Horario horario = usuario.getHorario();
                long minutosRetraso = ChronoUnit.MINUTES.between(
                        horario.getHoraEntrada(),
                        asistencia.getHoraEntrada()
                );
                return "Llegó tarde (" + minutosRetraso + " min de retraso)";
            case AUSENTE:
                return "Ausente";
            case JUSTIFICADO:
                return "Justificado";
            default:
                return "Estado desconocido";
        }
    }

    // Método para obtener archivos desde resources
    private File getFile(String filename) throws IOException {
        Resource resource = new ClassPathResource(filename);
        return resource.getFile();
    }


    public JasperPrint runReport() throws JRException, IOException {

        HashMap<String, Object> param = new HashMap<>();

        // Cargar el diseño del informe
        JasperDesign jdesign = JRXmlLoader.load(getFile("jasper/resumen_asis.jrxml"));
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