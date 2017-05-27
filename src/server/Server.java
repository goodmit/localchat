package server;

import exceptions.AuthFailException;
import filters.ChatFilter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {

    //private static volatile Server instance;
    private boolean closed = false;
    private Socket socket;

    private List<ClientHandler> clients;

    private List<ChatFilter> filters;

    private ServerSocket serverSocket = null;

    public void addFilter(ChatFilter filter) {
        filters.add(filter);
        System.out.println("Filter is added!");
    }

    synchronized void addClient(ClientHandler clientHandler, String nick) throws AuthFailException {
        for(ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                System.out.println("Client with nick " + nick + " is already exists!");
                throw new AuthFailException();
            }
        }
        clientHandler.setNick(nick);

        clients.add(clientHandler);
        System.out.println(clientHandler.getClientName() + " is added to autorized users!");
    }

    synchronized void removeClient(String nick, ClientHandler client) {
        clients.remove(client);
        System.out.println("Client " + nick + " disconnected.");
    }


    public Server(int serverPort, String dbName) {
        System.out.println("Server init start.");
        clients = new LinkedList<>();
        filters = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server socket init OK.");

            SQLHandler.connect(dbName);
            System.out.println("Server DB init OK.");

            System.out.println("Server ready and waiting for clients...");
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void waitForClient() {
        socket = null;
        try {
            while(!closed) {
                    socket = serverSocket.accept();
                    System.out.println("Client connected.");
                    ClientHandler client = new ClientHandler(socket, this);
                    new Thread(client).start();
                    if (socket == null) closed = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSession();
        }
    }

    synchronized void newMessageFromClient(String message, String clientName) {
        for (ChatFilter filter : filters) {
            message = filter.filter(message);
        }
        for(ClientHandler client : clients) {
            try {
                client.getOut().writeUTF(clientName + ": " + message);
                client.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            client.getOut().flush();
        }
    }

    public void closeSession() {
        if (!serverSocket.isClosed()) {
            System.out.println("Server is stopping now...");
            for (ClientHandler client : clients) {
                client.setClosed();
            }
            newMessageFromClient("Server was stopped", "SYSTEM");
            try {
                serverSocket.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Server stopped!");
        }
    }

    public void printClients() {
        for (ClientHandler client : clients) {
            System.out.println(client);
        }
    }
}