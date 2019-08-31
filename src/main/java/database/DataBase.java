package database;


import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import webservice.AppProperties;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.Properties;

public class DataBase {

    static Sql2o connection;
    public Table table;
    Properties properties = new Properties();

    public DataBase() {
        try {
            properties.load(Objects.requireNonNull(AppProperties.class.getClassLoader().getResourceAsStream("app.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToBD() {
        String server, port, login, password, service;
        server = properties.get("db.host").toString();
        port = properties.get("db.port").toString();
        login = properties.get("db.username").toString();
        password = properties.get("db.password").toString();
        service = properties.get("db.service").toString();

        connection = new Sql2o(String.format("jdbc:postgresql://%s:%s/%s", server, port, service), login, password);
    }
    public void connectToBD(String server, String port, String login, String password, String service) {

        connection = new Sql2o(String.format("jdbc:postgresql://%s:%s/%s", server, port, service), login, password);
    }

    public Table executeQuery(String request) {
        Connection con = connection.open();
        table = con.createQuery(request).executeAndFetchTable();
        return table;
    }

}
