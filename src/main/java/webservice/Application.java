package webservice;

import database.DataBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        DataBase db = new DataBase();
        db.connectToBD();
        db.setDBEnvironment();

        SpringApplication.run(Application.class, args);
    }
}
