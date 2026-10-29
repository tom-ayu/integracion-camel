package tomas.ontaneda.integracion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class MatriculasRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // noop=true conserva el archivo original y evita reprocesarlo
        from("file:input?include=.*\\.csv&noop=true&charset=UTF-8")
            .routeId("educonnect-matriculas")
            .log("INICIO | Archivo: ${header.CamelFileName}")

            .process(exchange -> {
                String csv = exchange.getMessage().getBody(String.class);
                List<Matricula> registros = MatriculaCsvParser.parse(csv);
                exchange.getMessage().setBody(registros);
                exchange.setProperty("totalLeidos", registros.size());
            })

            .process(this::validar)

            .process(this::calcularObligaciones)

            //transferencia de archivos al SF mediante CSV, sin acoplarse a su codigo
            .setBody(e -> csv("estudiante_id,nombre,concepto,valor_original,descuento,total",
                    e.getProperty("obligaciones", List.class), ObligacionFinanciera::toCsv))
            .to("file:output?fileName=obligaciones_financieras.csv&charset=UTF-8")

            .setBody(e -> csv("id_estudiante,nombre,motivo_error",
                    e.getProperty("rechazados", List.class), MatriculaRechazada::toCsv))
            .to("file:output?fileName=errores_matriculas.csv&charset=UTF-8")

            .log("RESUMEN | Archivo: ${header.CamelFileName} | Leidos: ${exchangeProperty.totalLeidos}"
                    + " | Validos: ${exchangeProperty.validos.size()} | Rechazados: ${exchangeProperty.rechazados.size()}");
    }

    //un registro invalido no detiene el procesamiento del resto
    private void validar(Exchange exchange) {
        @SuppressWarnings("unchecked")
        List<Matricula> registros = exchange.getMessage().getBody(List.class);
        MatriculaValidator validator = new MatriculaValidator();
        List<Matricula> validos = new ArrayList<>();
        List<MatriculaRechazada> rechazados = new ArrayList<>();

        for (Matricula m : registros) {
            validator.validar(m).ifPresentOrElse(
                    motivo -> {
                        rechazados.add(new MatriculaRechazada(m, motivo));
                        log.info("RECHAZO | id: {} | nombre: {} | motivo: {}", m.idEstudiante(), m.nombre(), motivo);
                    },
                    () -> validos.add(m));
        }

        exchange.setProperty("validos", validos);
        exchange.setProperty("rechazados", rechazados);
    }

    private void calcularObligaciones(Exchange exchange) {
        @SuppressWarnings("unchecked")
        List<Matricula> validos = exchange.getProperty("validos", List.class);
        List<ObligacionFinanciera> obligaciones = new ArrayList<>();

        for (Matricula m : validos) {
            ObligacionFinanciera o = ObligacionFinanciera.desde(m);
            obligaciones.add(o);
            log.info("OBLIGACION | id: {} | original: {} | descuento: {} | total: {}",
                    o.estudianteId(), o.valorOriginal().toPlainString(), o.descuento().toPlainString(), o.total().toPlainString());
        }

        exchange.setProperty("obligaciones", obligaciones);
    }

    private static <T> String csv(String cabecera, List<T> filas, Function<T, String> fila) {
        return filas.stream().map(fila).collect(Collectors.joining("\n", cabecera + "\n", "\n"));
    }
}
