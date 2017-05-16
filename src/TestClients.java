import client.ClientWindow;

public class TestClients {

    private static final String SERVER_HOST = "192.168.1.122"; //127.0.0.1
    private static final int SERVER_PORT = 9933;

    public static void main(String[] args) {
        new ClientWindow(SERVER_HOST, SERVER_PORT);
    }
}