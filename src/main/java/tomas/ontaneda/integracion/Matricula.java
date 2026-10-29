package tomas.ontaneda.integracion;

/*
        registro de matricula tal como viene en el CSV del SA 
        valores se guardan sin modificar para conservarlos en el reporte de errores
 */
public record Matricula(
        String idEstudiante,
        String nombre,
        String carrera,
        String valorMatricula,
        String beca,
        String estado) {
}
