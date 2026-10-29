package tomas.ontaneda.integracion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MatriculaValidator {

    private final Set<String> idsReservados = new HashSet<>();

    //devuelve el primer error/vacio si el registro es valido
    public Optional<MotivoError> validar(Matricula m) {
        String id = m.idEstudiante().trim();
        if (!esIdValido(id)) {
            return Optional.of(MotivoError.ID_INVALIDO);
        }
        //la primera aparicion reserva el id aunque la fila tenga otro error
        if (!idsReservados.add(new BigInteger(id).toString())) {
            return Optional.of(MotivoError.ID_DUPLICADO);
        }
        if (m.nombre().isBlank()) {
            return Optional.of(MotivoError.NOMBRE_VACIO);
        }
        if (m.carrera().isBlank()) {
            return Optional.of(MotivoError.CARRERA_VACIA);
        }
        if (!esValorValido(m.valorMatricula().trim())) {
            return Optional.of(MotivoError.VALOR_INVALIDO);
        }
        String beca = m.beca().trim();
        if (!beca.equals("S") && !beca.equals("N")) {
            return Optional.of(MotivoError.BECA_INVALIDA);
        }
        String estado = m.estado().trim();
        if (estado.equals("RETIRADO")) {
            return Optional.of(MotivoError.ESTUDIANTE_RETIRADO);
        }
        if (!estado.equals("MATRICULADO")) {
            return Optional.of(MotivoError.ESTADO_INVALIDO);
        }
        return Optional.empty();
    }

    private boolean esIdValido(String id) {
        return id.matches("\\d+") && new BigInteger(id).signum() > 0;
    }

    private boolean esValorValido(String valor) {
        try {
            return new BigDecimal(valor).signum() > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
