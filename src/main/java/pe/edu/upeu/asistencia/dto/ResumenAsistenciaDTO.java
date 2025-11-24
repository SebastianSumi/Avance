package pe.edu.upeu.asistencia.dto;

import lombok.Data;

@Data

public class ResumenAsistenciaDTO {
    private Long empleadoId;
    private String nombreEmpleado;
    private long totalPresente;
    private long totalTarde;
    private long totalAusente;
    private long totalJustificado;
    private long totalGeneral;
    private double porcentajeAsistencia;

    public ResumenAsistenciaDTO(Long empleadoId, String nombreEmpleado,
                                long totalPresente, long totalTarde,
                                long totalAusente, long totalJustificado) {
        this.empleadoId = empleadoId;
        this.nombreEmpleado = nombreEmpleado;
        this.totalPresente = totalPresente;
        this.totalTarde = totalTarde;
        this.totalAusente = totalAusente;
        this.totalJustificado = totalJustificado;
        this.totalGeneral = totalPresente + totalTarde + totalAusente + totalJustificado;

        // Calcular porcentaje (Presente + Justificado / Total * 100)
        if (this.totalGeneral > 0) {
            this.porcentajeAsistencia = ((double)(totalPresente + totalJustificado) / totalGeneral) * 100;
        } else {
            this.porcentajeAsistencia = 0.0;
        }
    }

    // Getters y Setters
    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public long getPresente() {
        return totalPresente;
    }

    public void setPresente(long totalPresente) {
        this.totalPresente = totalPresente;
    }

    public long getTarde() {
        return totalTarde;
    }

    public void setTarde(long totalTarde) {
        this.totalTarde = totalTarde;
    }

    public long getAusente() {
        return totalAusente;
    }

    public void setAusente(long totalAusente) {
        this.totalAusente = totalAusente;
    }

    public long getJustificado() {
        return totalJustificado;
    }

    public void setJustificado(long totalJustificado) {
        this.totalJustificado = totalJustificado;
    }

    public long getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(long totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public double getPorcentajeAsistencia() {
        return porcentajeAsistencia;
    }

    public void setPorcentajeAsistencia(double porcentajeAsistencia) {
        this.porcentajeAsistencia = porcentajeAsistencia;
    }

    public String getPorcentajeFormateado() {
        return String.format("%.1f%%", porcentajeAsistencia);
    }

}