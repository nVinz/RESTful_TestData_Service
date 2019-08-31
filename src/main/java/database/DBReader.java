package database;

import org.sql2o.data.Column;
import org.sql2o.data.Row;

import java.util.List;

public class DBReader extends DataBase {

    public DBReader(String server, String port, String login, String password, String service){
        super();
        connectToBD(server, port, login, password, service);
    }

    public String getHTMLTable(String table, String neededColumns){
        executeQuery(String.format("SELECT %s FROM %s", neededColumns, table));
        int tableRange = this.table.asList().size();
        List<Column> columns = this.table.columns();
        List<Row> rows = this.table.rows();
        StringBuilder html = new StringBuilder("<table><tr>");
        columns.forEach(c -> html.append(String.format("<th>%s</th>", c.getName())));
        html.append("</tr>");
        System.out.println(html.toString());
        for (int i = 0; i < rows.size(); i++){
            html.append("<tr>");
            for(int j = 0; j < columns.size(); j++) {
                html.append(String.format("<td>%s</td>", this.table.asList().get(i).get(columns.get(j).getName().toLowerCase()).toString()));
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }
}
