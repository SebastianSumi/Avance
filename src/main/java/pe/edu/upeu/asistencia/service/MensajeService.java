package pe.edu.upeu.asistencia.service;

import pe.edu.upeu.asistencia.model.Mensaje;
import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    //  CONSTRUCTOR
    public MensajeService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }


    public Mensaje enviarMensaje(Usuario emisor, Usuario receptor, String asunto, String contenido) {
        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setAsunto(asunto);
        mensaje.setContenido(contenido);
        mensaje.setLeido(false);

        return mensajeRepository.save(mensaje);
    }

    public List<Mensaje> obtenerMensajesRecibidos(Usuario receptor) {
        return mensajeRepository.findByReceptorOrderByFechaEnvioDesc(receptor);
    }


    public List<Mensaje> obtenerMensajesEnviados(Usuario emisor) {
        return mensajeRepository.findByEmisorOrderByFechaEnvioDesc(emisor);//aun falta implementar
    }

    public List<Mensaje> obtenerMensajesNoLeidos(Usuario receptor) {
        return mensajeRepository.findByReceptorAndLeidoFalseOrderByFechaEnvioDesc(receptor);
    }


    //public long contarMensajesNoLeidos(Usuario receptor) {
    //    return mensajeRepository.countByReceptorAndLeidoFalse(receptor);
    //}


    public void marcarComoLeido(Long mensajeId) {
        Optional<Mensaje> mensaje = mensajeRepository.findById(mensajeId);

        if (mensaje.isPresent()) {
            mensaje.get().setLeido(true);
            mensajeRepository.save(mensaje.get());
        }
    }

    public void marcarTodosComoLeidos(Usuario receptor) {
        List<Mensaje> mensajesNoLeidos = obtenerMensajesNoLeidos(receptor);

        for (Mensaje mensaje : mensajesNoLeidos) {
            mensaje.setLeido(true);
            mensajeRepository.save(mensaje);
        }
    }


    public List<Mensaje> obtenerConversacion(Usuario usuario1, Usuario usuario2) {
        return mensajeRepository.findConversacion(usuario1, usuario2);
    }

    public List<Mensaje> obtenerUltimosMensajes(Usuario receptor) {
        List<Mensaje> todos = mensajeRepository.findByReceptorOrderByFechaEnvioDesc(receptor);
        return todos.size() > 20 ? todos.subList(0, 20) : todos;
    }


    public void eliminar(Long id) {
        mensajeRepository.deleteById(id);
    }

    //public Optional<Mensaje> buscarPorId(Long id) {
    //    return mensajeRepository.findById(id);
    //}

    //public List<Mensaje> listarTodos() {
      //  return mensajeRepository.findAll();
    //}
}