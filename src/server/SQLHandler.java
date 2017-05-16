package server;

import java.sql.*;
import java.util.ArrayList;

class SQLHandler {
    private static Connection connection;

    static void connect(String dbName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    static String getNick(String login, String password) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "select nick " +
                        "from \"users\" " +
                        "where login = \"" + login + "\" " +
                        "and password = \"" + password + "\""
        );
        String nick = null;
        while (rs.next()) {
            nick = rs.getString("nick");
        }
        return nick == null ? null : nick + "(" + login + ")";
    }

    static boolean isValid(String data) throws SQLException {
        StringBuilder allowedChars = new StringBuilder();
        boolean forbidden;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT letter " +
                        "FROM \"letters\""
        );
        while (rs.next()) {
            allowedChars.append(rs.getString("letter"));
        }
        for (int i = 0; i < data.length(); i++) {
            forbidden = true;
            for(int j = 0; j < allowedChars.length(); j++) {
                if (data.charAt(i) == allowedChars.charAt(j)) {
                    forbidden = false;
                    break;
                }
            }
            if (forbidden) return false;
        }
        return true;
    }

    synchronized static void registrateUser(String nick, String login, String password) throws SQLException {
        Statement statement = connection.createStatement();
        String SQL = "INSERT INTO 'users' (nick, login, password) " +
                     "VALUES (\'" + nick + "\', \'" + login + "\', \'" + password + "\')";
        int result = statement.executeUpdate(SQL);
        System.out.println("Result of registration: " + result);
    }

    synchronized static void writeData(String table, String column, String data) throws SQLException {
        Statement statement = connection.createStatement();
        String SQL = "INSERT INTO " + table + " (" + column + ") " +
                "VALUES (\'" + data + "\')";
        int result = statement.executeUpdate(SQL);
        System.out.println("Result of registration: " + result);
    }

    synchronized static void writeData(String table, String column, char data) throws SQLException {
        Statement statement = connection.createStatement();
        String SQL = "INSERT INTO " + table + " (" + column + ") " +
                "VALUES (\'" + data + "\')";
        int result = statement.executeUpdate(SQL);
        System.out.println("Result of registration: " + result);
    }

    synchronized static void writeData(String table, String column, int data) throws SQLException {
        Statement statement = connection.createStatement();
        String SQL = "INSERT INTO " + table + " (" + column + ") " +
                "VALUES (\'" + data + "\')";
        int result = statement.executeUpdate(SQL);
        System.out.println("Result of registration: " + result);
    }

    synchronized static ArrayList<String> getLoginList() throws SQLException {
        ArrayList<String> loginList = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT login FROM 'users'"
        );
        while (rs.next()) {
            loginList.add(rs.getString("login"));
        }
        return loginList;
    }


}
