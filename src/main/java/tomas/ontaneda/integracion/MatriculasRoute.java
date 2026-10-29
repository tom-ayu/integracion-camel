package tomas.ontaneda.integracion;

import java.util.ArrayList;
import java.util.List;

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
}
