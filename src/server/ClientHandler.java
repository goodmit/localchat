package server;

import exceptions.AuthFailException;
import exceptions.JoinFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private static int clientsCount = 0;

    private final String delimiter = "##_";
    private final String quit = "quit";

    private boolean closed = false;
    private int reason;
    private DataOutputStream out;
    private DataInputStream in;
    private Boolean hasClient = true;
    private String clientName;
    private Server server;
    private Socket socket;

    ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            clientsCount++;
            clientName = "client" + clientsCount;

            System.out.println("Client \"" + clientName + "\" ready!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        parseCommand();
    }

    private boolean isAuthOk(String message) {
        System.out.println(clientName + "[NO AUTH]: " + message);

        if (message != null) {
            String[] parsedMessage = message.split(delimiter);
            if (parsedMessage.length == 3) {
                try {
                    processAuthMessage(parsedMessage);
                } catch (AuthFailException e) {
                    System.out.println("Error! Reason status: " + e.getReasonStatus());
                    reason = e.getReasonStatus();
                    return false;
                }
                return true;
            }
            if (parsedMessage.length == 4) {
                try {
                    processJoinMessage(parsedMessage);
                } catch (JoinFailException e) {
                    System.out.println("Error! Reason status: " + e.getReasonStatus());
                    reason = e.getReasonStatus();
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isQuit(String message) {
        return message.equalsIgnoreCase(quit);
    }

    private void handleMessage(String message) {
        System.out.println(clientName + ": " + message);
        new Thread(new MessagesSender(message.substring(3 + delimiter.length()), clientName, server)).start();
    }

    private void handleReg(String message) {
        reason = ErrorTypes.JOIN_OK;
        try {
            if (isAuthOk(message)) {
                System.out.println(clientName + " successfully joined Chat");
            } else {
                System.out.println(clientName + " failed the registration. reason status: " + reason);
            }
            out.writeUTF("join" + delimiter + reason);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAuth(String message) {
        reason = ErrorTypes.AUTH_OK;
        try {
            if (isAuthOk(message)) {
                System.out.println(clientName + " authorized in the chat!");
            } else {
                System.out.println(clientName + " failed the authorization. reason status: " + reason);
            }
            out.writeUTF("auth" + delimiter + reason);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseCommand() {
        while (hasClient) {
            if (closed) {
                hasClient = false;
                System.out.println(clientName + " was disconnected by system.");
                server.removeClient(clientName, this);
                break;
            }

            String message = null;
            try {
                message = in.readUTF();
            } catch (IOException e) {
                    e.printStackTrace();
            }
            if (message != null) {
                String[] parsedMessage = message.split(delimiter);
                switch (parsedMessage[0]) {
                    case "join":
                        handleReg(message);
                        break;
                    case "auth":
                        handleAuth(message);
                        break;
                    case "msg":
                        handleMessage(message);
                        break;
                    case quit:
                        hasClient = false;
                        System.out.println(clientName + " disconnected.");
                        new Thread(new MessagesSender(clientName + " disconnected", "system", server)).start();
                        server.removeClient(clientName, this);
                        break;
                    default:
                        System.out.println("Unhandled command from client " + clientName);
                        System.out.println(message);
                        break;
                }
            }
        }
    }

    private void processJoinMessage(String[] parsedMessage) throws JoinFailException {
        System.out.println("Join message from " + clientName);

        String login = parsedMessage[1];
        String password = parsedMessage[2];
        String nick = parsedMessage[3];
        ArrayList<String> logins;

        if (nick.length() < 3) {
            throw new JoinFailException(ErrorTypes.JOIN_NICK_SHORT);
        }

        if (nick.length() > 16) {
            throw new JoinFailException(ErrorTypes.JOIN_NICK_LONG);
        }

        if (login.length() < 3) {
            throw new JoinFailException(ErrorTypes.JOIN_LOGIN_SHORT);
        }

        if (login.length() > 16) {
            throw new JoinFailException(ErrorTypes.JOIN_LOGIN_LONG);
        }

        if (password.length() < 4) {
            throw new JoinFailException(ErrorTypes.JOIN_PASS_SHORT);
        }

        if (password.length() > 12) {
            throw new JoinFailException(ErrorTypes.JOIN_PASS_LONG);
        }

        try {
            if (!SQLHandler.isValid(nick)) {
                throw new JoinFailException(ErrorTypes.JOIN_NICK_INCORRECT);
            }
            if (!SQLHandler.isValid(login)) {
                throw new JoinFailException(ErrorTypes.JOIN_LOGIN_INCORRECT);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try{
            logins = SQLHandler.getLoginList();
            for (String dbLogin : logins) {
                if (login.equalsIgnoreCase(dbLogin)) {
                    System.out.println(login + " is busy!");
                    throw new JoinFailException(ErrorTypes.JOIN_LOGIN_BUSY);
                }
            }
            SQLHandler.registrateUser(nick, login, password);

        } catch (SQLException e1) {
            e1.printStackTrace();
            throw new JoinFailException();
        }
    }

    private void processAuthMessage(String[] parsedMessage) throws AuthFailException {
        System.out.println("Auth message from " + clientName);

        String login = parsedMessage[1];
        String password = parsedMessage[2];
        String nick;

        if (login.length() < 1 || login.length() > 16) {
            throw new AuthFailException(ErrorTypes.AUTH_LOGIN_INCORRECT);
        }

        try {
            nick = SQLHandler.getNick(login, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthFailException();
        }
        if(nick != null) {
            server.addClient(this, nick);
            return;
        }

        throw new AuthFailException(ErrorTypes.AUTH_INCORRECT);
    }

    void setClosed() {
        closed = true;
    }

    @Override
    public String toString(){
        return clientName + ", active=" + hasClient;
    }

    DataOutputStream getOut() {
        return out;
    }

    String getClientName() {
        return clientName;
    }

    void setNick(String nick) {
        this.clientName = nick;
    }
}
