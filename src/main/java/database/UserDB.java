package database;

import org.postgresql.util.PSQLException;
import org.sql2o.Sql2o;

import java.util.Properties;

public class UserDB extends DataBase {

    public UserDB() {
        super();
        connectToBD();
    }

    public UserDB(String server, String port, String login, String password, String service) {
        super();
        connectToBD(server, port, login, password, service);
    }

    public void connectToBD(String server, String port, String login, String password, String service) {
        Properties properties = new Properties();
        connection = new Sql2o(String.format("jdbc:postgresql://%s:%s/%s", server, port, service), login, password);
    }

    // возвращает роль пользователя (или None) по его имени для указанного проекта
    public String getRole(String user, String project) {
        try {
            executeQuery(String.format("select \"Role\" from login where \"Name\" = '%s' and \"Project\" = '%s'", user, project));
        } catch (PSQLException e) {
            e.printStackTrace();
        }
        if (this.table.rows().size() == 0) {
            return "None";
        }
        return this.table.rows().get(0).getString(0);
    }
}
