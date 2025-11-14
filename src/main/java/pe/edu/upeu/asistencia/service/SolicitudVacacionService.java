package pe.edu.upeu.asistencia.service;

import pe.edu.upeu.asistencia.model.SolicitudVacacion;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.enums.EstadoSolicitud;
import pe.edu.upeu.asistencia.repository.SolicitudVacacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudVacacionService {

    private final SolicitudVacacionRepository solicitudRepository;

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

    //public List<SolicitudVacacion> listarPendientes() {
      //  return solicitudRepository.findByEstadoOrderByFechaSolicitudAsc(EstadoSolicitud.PENDIENTE);
    //}

    public List<SolicitudVacacion> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    //public Optional<SolicitudVacacion> buscarPorId(Long id) {
      //  return solicitudRepository.findById(id);
    //}

    //public long contarPendientesPorEmpleado(Usuario empleado) {
      //  return solicitudRepository.countByEmpleadoAndEstado(empleado, EstadoSolicitud.PENDIENTE);
    //}

    //public void eliminar(Long id) {
      //  solicitudRepository.deleteById(id);
    //}
}