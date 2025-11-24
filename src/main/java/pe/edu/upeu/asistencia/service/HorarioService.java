package pe.edu.upeu.asistencia.service;

import pe.edu.upeu.asistencia.model.Horario;
import pe.edu.upeu.asistencia.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    // Crear horario
    public Horario crearHorario(String nombre, LocalTime horaEntrada, LocalTime horaSalida, Integer toleranciaMinutos) {
        Horario horario = new Horario();
        horario.setNombre(nombre);
        horario.setHoraEntrada(horaEntrada);
        horario.setHoraSalida(horaSalida);
        horario.setToleranciaMinutos(toleranciaMinutos != null ? toleranciaMinutos : 15);
        horario.setActivo(true);
        return horarioRepository.save(horario);
    }

    // Listar todos los horarios
    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    // Listar horarios activos
    public List<Horario> listarActivos() {
        return horarioRepository.findByActivoTrue();
    }

    // Buscar por ID
    public Optional<Horario> buscarPorId(Long id) {
        return horarioRepository.findById(id);
    }

    // Actualizar horario
    public Horario actualizarHorario(Long id, String nombre, LocalTime horaEntrada, LocalTime horaSalida, Integer toleranciaMinutos) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setNombre(nombre);
        horario.setHoraEntrada(horaEntrada);
        horario.setHoraSalida(horaSalida);
        horario.setToleranciaMinutos(toleranciaMinutos);

        return horarioRepository.save(horario);
    }

    // Eliminar horario
    public void eliminarHorario(Long id) {
        horarioRepository.deleteById(id);
    }

    // Activar/Desactivar horario
    public Horario toggleActivo(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setActivo(!horario.getActivo());
        return horarioRepository.save(horario);
    }
}