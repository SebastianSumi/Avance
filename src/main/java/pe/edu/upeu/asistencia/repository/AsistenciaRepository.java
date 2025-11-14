package pe.edu.upeu.asistencia.repository;

import pe.edu.upeu.asistencia.model.Asistencia;
import pe.edu.upeu.asistencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {


    //busca asistencia de usu
    List<Asistencia> findByUsuario(Usuario usuario);

    //por fechaa
    List<Asistencia> findByFecha(LocalDate fecha);

    //fech y usua
    List<Asistencia> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
}