package tomas.ontaneda.integracion;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class FileTransferRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        from("file:input?include=.*\\.csv&readLock=changed&delete=true")
            .routeId("tiendasol-entrada")
            .log("INICIO | ${date:now:yyyy-MM-dd HH:mm:ss} | Procesando: ${header.filename}")
            
            .filter(header(Exchange.FILE_NAME).endsWith(".csv"))
            
            .process(exchange -> transformarProducto(exchange))
            
            .log("SALIDA | ${date:now:yyyy-MM-dd HH:mm:ss} | Archivo procesado")
            .to("file:output?charset=UTF-8")
            
            .to("direct:archivar");
        
        from("direct:archivar")
            .routeId("tiendasol-archivado")
            .to("file:archived?charset=UTF-8")
            .log("ARCHIVADO | ${date:now:yyyy-MM-dd HH:mm:ss} | Copia guardada");
    }

    private void transformarProducto(Exchange exchange) throws Exception {
        String csvContent = exchange.getMessage().getBody(String.class);
        
        String[] lines = csvContent.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (i == 0) {
                result.append(line).append("\n");
            } else if (!line.trim().isEmpty()) {
                String[] campos = line.split(",");
                if (campos.length >= 2) {
                    campos[1] = campos[1].toUpperCase();
                }
                result.append(String.join(",", campos)).append("\n");
            }
        }
        
        exchange.getMessage().setBody(result.toString());
    }
}