package tomas.ontaneda.integracion;

public record MatriculaRechazada(Matricula matricula, MotivoError motivo) {

    //conserva id y nombre originales aunque esten vacios
    public String toCsv() {
        return String.join(",", matricula.idEstudiante(), matricula.nombre(), motivo.name());
    }
}
