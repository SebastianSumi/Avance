package pe.edu.upeu.asistencia.service;

import pe.edu.upeu.asistencia.model.Usuario;
import pe.edu.upeu.asistencia.enums.Rol;
import pe.edu.upeu.asistencia.repository.UsuarioRepository;
import pe.edu.upeu.asistencia.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Usuario> autenticar(String username, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
        if (usuario.isPresent()) {
            String passwordHash = HashUtil.sha256(password);
            if (usuario.get().getPasswordHash().equals(passwordHash) && usuario.get().getActivo()) {
                return usuario;
            }
        }
        return Optional.empty();
    }

    public Usuario guardarUsuario(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setPasswordHash(HashUtil.sha256(usuario.getPasswordHash()));
        }
        return usuarioRepository.save(usuario);
    }

    public void actualizarUsuario(Usuario usuario, String nuevaPassword) {
        if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
            usuario.setPasswordHash(HashUtil.sha256(nuevaPassword));
        }
        usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    //public Optional<Usuario> buscarPorId(Long id) {
      //  return usuarioRepository.findById(id);
    //}

    //public Optional<Usuario> buscarPorUsername(String username) {
      //  return usuarioRepository.findByUsername(username);
    //}

    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    //public List<Usuario> listarPorRol(Rol rol) {
      //  return usuarioRepository.findByRol(rol);
    //}

    public void crearUsuario(String nombre, String username, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setPasswordHash(HashUtil.sha256(password)); // Hashear la contraseña
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}