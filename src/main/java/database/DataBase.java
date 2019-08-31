package database;


import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import java.sql.DriverManager;
import java.util.Properties;

public class DataBase {

    static Sql2o connection;
    public Table table;

    public void connectToBD() {
        Properties properties = new Properties();
        String server, port, login, password, service;

        server = properties.get("db.host").toString();
        port = properties.get("db.port").toString();
        login = properties.get("db.username").toString();
        password = properties.get("db.password").toString();
        service = properties.get("db.service").toString();

        connection = new Sql2o("jdbc:postgresql://localhost:5432/example", "postgres", "postgres");
    }

    public void executeQuery(String request){
        Connection con = connection.open();
        table = con.createQuery(request).executeAndFetchTable();

    }

}
