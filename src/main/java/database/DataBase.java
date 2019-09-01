package database;


import org.postgresql.util.PSQLException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import webservice.AppProperties;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

    // connect к БД по данным из app.properties
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

    public Table executeQuery(String request) throws PSQLException {
        Connection con = connection.open();
        table = con.createQuery(request).executeAndFetchTable();
        con.close();
        return table;
    }

    // insert/update/drop запросы
    public void executeQueryWithoutResult(String request) throws PSQLException {
        Connection con = connection.open();
        con.createQuery(request).executeUpdate();
        con.close();
    }

    public boolean isTableExists(String tableName) {
        String request = String.format("SELECT to_regclass('%s')", tableName);
        try{
            table = this.executeQuery(request);
        }
        catch (PSQLException e) {
            System.out.println("Error occured while request " + e.getMessage());
            return false;
        }
        return table.rows().get(0).getString(0) != null;
    }

    public boolean setDBEnvironment() {
        String requestToCreateLoginDB = "CREATE TABLE login (user text, project text, role text, hash text)";
        String requestToCreateMasksDB = "CREATE TABLE masks (column text, mask text, id bigint)";
        String requestToCreateTablesDB = "CREATE TABLE tables (id bigint, name text, project text, description text)";

        return makeExecutionIfDBNotExist(requestToCreateLoginDB, "login") && makeExecutionIfDBNotExist(requestToCreateMasksDB, "masks") && makeExecutionIfDBNotExist(requestToCreateTablesDB, "tables");
    }

    private boolean makeExecutionIfDBNotExist(String request, String tableName){
        DataBase db = new DataBase();
        db.connectToBD();
        if (!db.isTableExists(tableName)) {
            try {
                db.executeQueryWithoutResult(request);
            }
            catch (PSQLException e) {
                System.out.println("Error occurred while request " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
