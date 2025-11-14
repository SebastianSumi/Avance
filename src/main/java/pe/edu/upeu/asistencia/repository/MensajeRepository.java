package pe.edu.upeu.asistencia.repository;

import pe.edu.upeu.asistencia.model.Mensaje;
import pe.edu.upeu.asistencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Mensajes recibidos
    List<Mensaje> findByReceptorOrderByFechaEnvioDesc(Usuario receptor);

    // Mensajes enviados
    List<Mensaje> findByEmisorOrderByFechaEnvioDesc(Usuario emisor);

    // Mensajes no leídos
    List<Mensaje> findByReceptorAndLeidoFalseOrderByFechaEnvioDesc(Usuario receptor);

    // Contar mensajes no leídos
    long countByReceptorAndLeidoFalse(Usuario receptor);

    // Mensajes entre dos usuarios
    @Query("SELECT m FROM Mensaje m WHERE (m.emisor = :usuario1 AND m.receptor = :usuario2) OR (m.emisor = :usuario2 AND m.receptor = :usuario1) ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findConversacion(@Param("usuario1") Usuario usuario1, @Param("usuario2") Usuario usuario2);

    // Mensajes recibidos
    @Query("SELECT m FROM Mensaje m WHERE m.receptor = :receptor ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findTop20ByReceptorOrderByFechaEnvioDesc(@Param("receptor") Usuario receptor);
}