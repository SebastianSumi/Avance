package pe.edu.upeu.asistencia.config;  // ✅ CAMBIA ESTO

import pe.edu.upeu.asistencia.model.Usuario;

public class SessionManager {
    //Es como memoria temporal de la sesión

    private static SessionManager instance;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    //public boolean hayUsuarioLogueado() {
      //  return usuarioActual != null;
    //}
}