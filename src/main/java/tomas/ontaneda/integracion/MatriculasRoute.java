package tomas.ontaneda.integracion;

import java.util.List;

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

            .log("LECTURA | Archivo: ${header.CamelFileName} | Registros leidos: ${exchangeProperty.totalLeidos}");
    }
}
