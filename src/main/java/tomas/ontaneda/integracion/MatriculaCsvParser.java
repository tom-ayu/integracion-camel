package tomas.ontaneda.integracion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MatriculaCsvParser {

    private static final int COLUMNAS = 6;

    private MatriculaCsvParser() {
    }

    /*
        convierte el contenido CSV en registros y en orden de entrada
        omite cabecera y lineas vacias
        las filas con menos columnas se completan con vacios para que la validacion las rechace
    */
    public static List<Matricula> parse(String csv) {
        List<Matricula> registros = new ArrayList<>();
        if (csv == null || csv.isEmpty()) {
            return registros;
        }

        String[] lineas = csv.split("\\r?\\n");
        for (int i = 1; i < lineas.length; i++) {
            if (lineas[i].isBlank()) {
                continue;
            }
            String[] campos = Arrays.copyOf(lineas[i].split(",", -1), COLUMNAS); // limite -1 para no perder campos vacios al final de la linea
            registros.add(new Matricula(
                    valor(campos[0]),
                    valor(campos[1]),
                    valor(campos[2]),
                    valor(campos[3]),
                    valor(campos[4]),
                    valor(campos[5])));
        }
        return registros;
    }

    private static String valor(String campo) {
        return campo == null ? "" : campo;
    }
}
