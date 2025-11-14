package pe.edu.upeu.asistencia.service;

import pe.edu.upeu.asistencia.model.Asistencia;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.repository.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public Asistencia registrarAsistencia(Usuario usuario) {
        // Verificar si ya registró hoy
        List<Asistencia> asistenciasHoy = asistenciaRepository.findByUsuarioAndFecha(usuario, LocalDate.now());

        if (!asistenciasHoy.isEmpty()) {
            throw new RuntimeException("Ya existe un registro de asistencia para hoy");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(LocalDate.now());
        asistencia.setHoraEntrada(LocalTime.now());
        asistencia.setEstado("PRESENTE");

        return asistenciaRepository.save(asistencia);
    }

    public Asistencia registrarSalida(Long asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));// aun no se hace

        asistencia.setHoraSalida(LocalTime.now());
        return asistenciaRepository.save(asistencia);
    }

    public List<Asistencia> listarPorUsuario(Usuario usuario) {
        return asistenciaRepository.findByUsuario(usuario);
    }

    public List<Asistencia> listarPorFecha(LocalDate fecha) {
        return asistenciaRepository.findByFecha(fecha);
    }


    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }


    public Asistencia buscarPorId(Long id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
    }

    public void eliminarAsistencia(Long id) {
        asistenciaRepository.deleteById(id);
    }

    public boolean yaRegistroHoy(Usuario usuario) {
        List<Asistencia> asistenciasHoy = asistenciaRepository.findByUsuarioAndFecha(usuario, LocalDate.now());
        return !asistenciasHoy.isEmpty();
    }
}