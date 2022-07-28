package com.geekbrains.cloud.june.regin.Registration;


import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//новый класс для работы с psql
public class JdbcApp {
    private static Connection connection;
    private static Statement statement;
    public static List<ClientInfo> logins;


    public static void main() {
        try {
            connect();
            logins = findAll();
        } catch (SQLException e) {
            log.debug(String.valueOf(e));     //logger hw3-6-3*
        }
    }

    public static void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.debug(String.valueOf(e));           //logger hw3-6-3*
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.debug(String.valueOf(e));           //logger hw3-6-3*
        }
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cloud", "postgres", "postgres");
        statement = connection.createStatement();
    }

    private static void createTableEx() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXIST auth(\n+" +
                "login varchar(64) PRIMARY KEY UNIQUE, \n+" +
                "password varchar(64),\n+" +
                ");");
    }

    private static void dropeTable() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXIST auth;");
    }

    private static void readEx() throws SQLException {
        try (ResultSet rs = statement.executeQuery("select * from auth ORDER BY login ASC ;")) {
            while (rs.next()) {
                log.info(rs.getString("login") + " " + rs.getString("password"));
            }
        }
    }


    private static void clearTableEx() throws SQLException {
        statement.executeUpdate("DELETE FROM auth;");
    }

    public static void insertEx(String login, String password) throws SQLException {
        statement.executeUpdate("INSERT INTO auth (login, password) \n" +
                "VALUES ('" + login + "','" + password + "'); ");

    }

    private static List<ClientInfo> findAll() throws SQLException {
        logins = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("select * from auth ORDER BY login ASC ;")) {
            while (rs.next()) {
                ClientInfo client = new ClientInfo(rs.getString(1), rs.getString(2));
                        logins.add(client);
            }
        }

        return logins;
    }

}
