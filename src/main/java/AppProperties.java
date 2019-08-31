import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class AppProperties {
    private boolean logBody;
    private String host = "127.0.0.1";
    private int port = 8080;
    private static final AppProperties instance = new AppProperties();

    private AppProperties() {
        Properties properties = new Properties();

        try {
            properties.load(Objects.requireNonNull(AppProperties.class.getClassLoader().getResourceAsStream("app.properties")));
            logBody = Boolean.parseBoolean(properties.get("app.log.body").toString());
            host = properties.getOrDefault("app.default.host", host).toString();
            port = Integer.parseInt(properties.getOrDefault("app.default.port", port).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLogBody() {
        return logBody;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static AppProperties getInstance() {
        return instance;
    }

    void setHost(String host) {
        this.host = host;
    }

    void setPort(int port) {
        this.port = port;
    }

    void addApplicationArgs(String[] args) {
        host = args.length > 0 ? args[0] : host;
        try {
            port = args.length > 1 ? Integer.parseInt(args[1]) : port;
        } catch (NumberFormatException e) {
            //
        }
    }
}
