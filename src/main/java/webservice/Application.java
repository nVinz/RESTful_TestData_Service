package webservice;

import database.DataBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

@SpringBootApplication
public class Application {

    private static ConfigurableApplicationContext context = null;

    public static void main(String[] args) {
        AppProperties.getInstance().addApplicationArgs(args);

        SpringApplication application = new SpringApplication(Application.class);

        Properties properties = new Properties();
        properties.put("server.port", AppProperties.getInstance().getPort());
        properties.put("server.address", AppProperties.getInstance().getHost());
        application.setDefaultProperties(properties);

        DataBase db = new DataBase();
        db.connectToBD();
        db.setDBEnvironment();

        context = application.run(args);
    }
}
