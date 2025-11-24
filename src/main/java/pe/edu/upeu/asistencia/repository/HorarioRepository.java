package pe.edu.upeu.asistencia.repository;

import pe.edu.upeu.asistencia.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    // Buscar horarios activos
    List<Horario> findByActivoTrue();

    // Buscar por nombre
    List<Horario> findByNombreContainingIgnoreCase(String nombre);
}