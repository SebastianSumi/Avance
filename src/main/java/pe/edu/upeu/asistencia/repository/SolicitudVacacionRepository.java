package pe.edu.upeu.asistencia.repository;

import pe.edu.upeu.asistencia.model.SolicitudVacacion;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudVacacionRepository extends JpaRepository<SolicitudVacacion, Long> {

    List<SolicitudVacacion> findByEmpleadoOrderByFechaSolicitudDesc(Usuario empleado);

    List<SolicitudVacacion> findByEstadoOrderByFechaSolicitudAsc(EstadoSolicitud estado);

    List<SolicitudVacacion> findAllByOrderByFechaSolicitudDesc();

    long countByEmpleadoAndEstado(Usuario empleado, EstadoSolicitud estado);
}
