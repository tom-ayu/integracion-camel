package tomas.ontaneda.integracion;

import org.apache.camel.main.Main;

public class Application {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new FileTransferRoute());
        main.run();
    }
}