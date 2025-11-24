package pe.edu.upeu.asistencia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "horario")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Turno Mañana", "Turno Tarde"

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "tolerancia_minutos")
    private Integer toleranciaMinutos = 15; // Minutos de tolerancia para llegar tarde

    @Column(nullable = false)
    private Boolean activo = true;

    @Override
    public String toString() {
        return nombre + " (" + horaEntrada + " - " + horaSalida + ")";
    }
}