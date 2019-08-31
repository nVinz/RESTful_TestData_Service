package database;

import org.sql2o.Sql2o;

import java.util.Properties;

public class UserDB extends DataBase {

    public UserDB() {
        super();
        connectToBD();
    }


    public void connectToBD(String server, String port, String login, String password, String service) {
        Properties properties = new Properties();
        connection = new Sql2o(String.format("jdbc:postgresql://%s:%s/%s", server, port, service), login, password);
    }

    public boolean checkRole(String project, String user) {
        executeQuery(String.format("select count(*) from login where name = %s and role = %s", user, project));
        String value = table.asList().get(0).get("count").toString();
        return false;
    }
}
