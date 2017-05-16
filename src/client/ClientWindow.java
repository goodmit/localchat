package client;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ClientWindow extends JFrame {

    private Boolean end = false;

    private JPanel authPanel;
    private JPanel chatPanel;
    private JPanel connectPanel;
    private JPanel gamePanel;
    private JPanel joinPanel;

    private JTextField clientMsgElement;
    private JTextArea serverMsgElement;

    private final String delimiter = "##_";
    private final String quit = "quit";

    final String serverHost;
    final int serverPort;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public ClientWindow(String host, int port) {
        serverHost = host;
        serverPort = port;

        initGUI();
    }

    private void initConnection(String serverHost, int serverPort) throws IOException {
        socket = new Socket(serverHost, serverPort);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void initGUI() {
        setBounds(600, 300, 800, 500);
        setTitle("Chat Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatPanel = new JPanel(new BorderLayout());

        //for auth
        authPanel = new JPanel(new GridLayout(2,3));

        JTextField jtfLogin = new JTextField();             // Login
        jtfLogin.setToolTipText("Enter your Login here");

        JPasswordField jtfPass = new JPasswordField();      // Password
        jtfPass.setToolTipText("Enter your Password here");

        JButton jbAuth = new JButton("Log In");        // Button 'Log In'
        jbAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendAuthCommand(
                            jtfLogin.getText().equalsIgnoreCase("") ? " " : jtfLogin.getText(),
                            jtfPass.getPassword()
                    );
                    serverMsgElement.append("HASH пароля: " + Arrays.hashCode(jtfPass.getPassword()) + "\n");
                    jtfLogin.setText("");
                    jtfPass.setText("");
                    joinPanel.setVisible(false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton jbJoin = new JButton("Join");           // Button 'Join'
        jbJoin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinPanel.setVisible(!joinPanel.isVisible());
            }
        });

        authPanel.add(new JLabel("Login: "));
        authPanel.add(jtfLogin);
        authPanel.add(jbAuth);
        authPanel.add(new JLabel("Password: "));
        authPanel.add(jtfPass);
        authPanel.add(jbJoin);

        chatPanel.add(authPanel, BorderLayout.NORTH);
        joinChat();

        // поле, отображающее сообщения чата
        serverMsgElement = new JTextArea();
        serverMsgElement.setEditable(false);
        serverMsgElement.setLineWrap(true);

        // контейнер, позволяющий скроллить компонент serverMsgElement
        JScrollPane scrollPane = new JScrollPane(serverMsgElement);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);


        //кнопка отправки сообщения
        JButton sendButton = new JButton("SEND");
        bottomPanel.add(sendButton, BorderLayout.EAST);
        clientMsgElement = new JTextField();
        clientMsgElement.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
//                if (str == null)
//                    return;
                if ((getLength() + str.length()) <= 400) {
                    super.insertString(offset, str, attr);
                }
            }
        });


        bottomPanel.add(clientMsgElement, BorderLayout.CENTER);

        //отправка по кнопке
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientMsgElement.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        sendMessage("msg" + delimiter + message);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                clientMsgElement.grabFocus();
            }
        });

        //отправка по Enter
        clientMsgElement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientMsgElement.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        sendMessage("msg" + delimiter + message);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        // закрытие окна приложения клиента
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                closeSession();
            }
        });

        add(chatPanel, BorderLayout.CENTER);

        initConnectPanel();
        initGamePanel();

        setVisible(true);
    }

    private void sendAuthCommand(String login, char[] pass) throws IOException {
        String password = String.valueOf(Arrays.hashCode(pass));
        String command = "auth" + delimiter + login + delimiter + password;
        out.writeUTF(command);
        out.flush();
    }

    private void sendJoinCommand(String login, String password, String nick) throws IOException {
        String command = "join" + delimiter + login + delimiter + password + delimiter + nick;
        out.writeUTF(command);
        out.flush();
    }

    private void initConnectPanel() {
        connectPanel = new JPanel(new GridLayout(1,5));

        JTextField jtfHost = new JTextField();             // ServerHost
        jtfHost.setToolTipText("Type server ip-address here");

        JTextField jtfPort = new JTextField();      // ServerPort
        jtfPort.setToolTipText("Type server port here");

        JButton jbConnect = new JButton("Connect");           // Button 'Join'
        jbConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverMsgElement.append("Соединение с сервером...\n");
                    initConnection(jtfHost.getText(), Integer.parseInt(jtfPort.getText()));
                    initServerListner();
                    serverMsgElement.append("Соединение с \'" + jtfHost.getText() + "\' установлено!\n");
                    connectPanel.setVisible(false);
                    chatPanel.add(authPanel, BorderLayout.NORTH);
                } catch (NumberFormatException e1) {
                    //e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Incorrect server port!\n" +
                            "Please fill it correctly!");
                } catch (ConnectException e2) {
                    //e2.printStackTrace();
                    serverMsgElement.append("Нет соединения с сервером! Попробуйте указать другой ip-адрес или порт\n");
                } catch (UnknownHostException e3) {
                    //e3.printStackTrace();
                    serverMsgElement.append("Указан неверный адрес сервера! Попробуйте указать другой ip-адрес\n");
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        });

        connectPanel.add(new JLabel("Server host: "));
        connectPanel.add(jtfHost);
        connectPanel.add(new JLabel("Server port: "));
        connectPanel.add(jtfPort);
        connectPanel.add(jbConnect);

        jtfHost.setText(serverHost);
        jtfPort.setText(String.valueOf(serverPort));

        chatPanel.add(connectPanel, BorderLayout.NORTH);
    }

    private void initGamePanel() {
        gamePanel = new JPanel(new BorderLayout());

        JButton jbSelect = new JButton("Select Game");           // Button Select Game
        jbSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Come Soon!");
            }
        });

        gamePanel.add(jbSelect, BorderLayout.NORTH);

        gamePanel.setVisible(false);
        add(gamePanel, BorderLayout.EAST);

    }

    private void initServerListner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!end) {
                            String message = in.readUTF();
                            String[] parsedMessage = message.split(delimiter);
                            switch (parsedMessage[0]) {
                                case "join":
                                    handleReg(parsedMessage);
                                    break;
                                case "auth":
                                    handleAuth(parsedMessage);
                                    break;
                                default:
                                    if (message.equalsIgnoreCase("end session")) {
                                        end = true;
                                        closeSession();
                                        break;
                                    } else
                                        serverMsgElement.append(message + "\n");
                                    break;
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Клиент ушел в небытие!");
                }
            }
        }).start();
    }

    private void handleReg(String[] message) {
        switch (message[1]) {
            case "100":
                JOptionPane.showMessageDialog(null, "Registration completed! " +
                        "You should Log In to enter the Chat");
                break;
            case "101":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Login is busy!");
                break;
            case "102":
            case "103":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Login must contain from 3 to 16 letters!");
                break;
            case "104":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Login contains invalid symbols");
                break;
            case "105":
            case "106":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Password must contain from 6 to 40 letters!");
                break;
            case "107":
            case "108":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Nickname must contain from 3 to 16 letters!");
                break;
            case "109":
                JOptionPane.showMessageDialog(null, "Registration failed! " +
                        "ERROR! Nickname contains invalid symbols");
                break;
            default:
                break;
        }
    }

    private void handleAuth(String[] message) {
        switch (message[1]) {
            case "200":
                JOptionPane.showMessageDialog(null, "Autorization completed!");
                clientMsgElement.setText("");
                serverMsgElement.setText("Welcome to the Chat!\n");
                authPanel.setVisible(false);
                gamePanel.setVisible(true);
                break;
            case "201":
            case "202":
                JOptionPane.showMessageDialog(null, "Autorization failed! " +
                        "Incorrect login or password!");
                break;
        }
    }

    private void joinChat() {
        joinPanel = new JPanel(new GridLayout(9,1));
        JTextField jtfNick = new JTextField();
        joinPanel.add(new JLabel("Nickname (will be displayed in the Chat): "));
        jtfNick.setToolTipText("Can contain English and Russian letters, digits and symbols: \'-\' \'_\'");
        joinPanel.add(jtfNick);

        JTextField jtfLogin = new JTextField();
        joinPanel.add(new JLabel("Login: "));
        jtfLogin.setToolTipText("Only English or Russian letters and digits");
        joinPanel.add(jtfLogin);

        JPasswordField jtfPass = new JPasswordField();
        joinPanel.add(new JLabel("Password: "));
        jtfPass.setToolTipText("Required to enter in the Chat");
        joinPanel.add(jtfPass);

        JPasswordField jtfConfirm = new JPasswordField();
        joinPanel.add(new JLabel("Confirm Password: "));
        jtfConfirm.setToolTipText("Retype the password");
        joinPanel.add(jtfConfirm);

        JButton jbRegistrate = new JButton("Registration");
        joinPanel.add(jbRegistrate);

        joinPanel.setVisible(false);
        chatPanel.add(joinPanel, BorderLayout.EAST);

        // отправка данных для регистрации по кнопке JOIN
        jbRegistrate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkJoinInfo(
                        jtfNick.getText().equalsIgnoreCase("") ? " " : jtfNick.getText(),
                        jtfLogin.getText().equalsIgnoreCase( "") ? " " : jtfLogin.getText(),
                        jtfPass.getPassword().length > 5 ? jtfPass.getPassword() : null,
                        jtfConfirm.getPassword().length > 5 ? jtfPass.getPassword() : null
                );

                joinPanel.setVisible(false);
            }
        });
    }

    private void checkJoinInfo(String nick, String login, char[] pass, char[] confirm) {
       if (!Arrays.equals(pass, confirm)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Fail registration. Password not confirmed"
            );
        } else {
            try {
                String password = String.valueOf(Arrays.hashCode(pass));
                sendJoinCommand(login, password, nick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSession() {
        try {
            if(out != null) {
                out.writeUTF(quit);
                out.flush();
                out.close();
            }
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) throws IOException {
        if (!message.trim().isEmpty()) {
            try {
                out.writeUTF(message);
                out.flush();
                clientMsgElement.setText("");
            } catch (NullPointerException e) {
                //e.printStackTrace();
                clientMsgElement.setText("");
            }
        }
    }
}
