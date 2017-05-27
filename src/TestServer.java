import filters.ChairOnlyFilter;
import filters.JavaOnlyFilter;
import server.Server;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Scanner;

public class TestServer {

    //public static final String SERVER_HOST = "localhost"; //127.0.0.1
    private static final int SERVER_PORT = 9933;
    private static final String DB_NAME = "chat.db";

    private static Scanner scanner;
    private static Server server;


    public static void main(String[] args) {
        server = new Server(SERVER_PORT, DB_NAME);

        //чтобы основной поток не "замирал" в этом месте
        new Thread(server::waitForClient).start();
        server.addFilter(new JavaOnlyFilter());
        server.addFilter(new ChairOnlyFilter());
        //и т.д. - по ходу программы можем добавлять новые фильтры


        scanner = new Scanner(new InputStreamReader(System.in));
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if(commandHandle(command) < 0) {
                server.closeSession();
                return;
            }
        }

    }

    private static int commandHandle(String command) {
        switch (command) {
            case "stop":
                return -1;
            case "game":
                return 0;
            case "status":
                return 0;
            case "clients":
                server.printClients();
                return 0;
            default:
                System.out.println("Warning! Unknown command!");
                return 0;
        }
    }


}