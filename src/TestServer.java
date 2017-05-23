import filters.ChairOnlyFilter;
import filters.JavaOnlyFilter;
import server.Server;

public class TestServer {

    //public static final String SERVER_HOST = "localhost"; //127.0.0.1
    private static final int SERVER_PORT = 9933;
    private static final String DB_NAME = "chat.db";

    public static void main(String[] args) {
        Server server = new Server(SERVER_PORT, DB_NAME);

        //чтобы основной поток не "замирал" в этом месте
        new Thread(server::waitForClient).start();
        server.addFilter(new JavaOnlyFilter());
        server.addFilter(new ChairOnlyFilter());
        //и т.д. - по ходу программы можем добавлять новые фильтры
    }

    private void commandHandle(String command) {
        switch (command) {
            case "stop":
                break;
            case "game":
                break;
            case "status":
                break;
            default:
                break;
        }
    }
}