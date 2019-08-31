package database;

import org.postgresql.util.PSQLException;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import java.util.List;

public class DBReader extends DataBase {

    public DBReader(String server, String port, String login, String password, String service) {
        super();
        connectToBD(server, port, login, password, service);
    }
    public DBReader(){
        super();
        connectToBD();
    }

    public String getHTMLTable(String table, String neededColumns) {
        try {
            executeQuery(String.format("SELECT %s FROM %s", neededColumns, table));
        } catch (PSQLException e) {
            System.out.println("Error occured while request " + e.getMessage());
            return "";
        }
         return buildHTMLTableByResult();
    }

    public String getFilteredHTMLTable(String table, String neededColumns, String filteredColumn, String mask) {
        if (!mask.equals(""))
            mask = String.format(" WHERE %s like '%s'", filteredColumn, mask);
//        if (mask.charAt(0) != '^') {
//            mask = "%" + mask;
//        }
//        if (mask.charAt(mask.length() - 1) != '$'){
//            mask = mask + "%";
//        }
        try {
            executeQuery(String.format("SELECT %s FROM %s%s", neededColumns, table, mask));
        } catch (PSQLException e) {
            System.out.println("Error occured while request " + e.getMessage());
            return "";
        }

        return buildHTMLTableByResult();

    }

    private String buildHTMLTableByResult(){
        List<Column> columns = this.table.columns();
        List<Row> rows = this.table.rows();
        StringBuilder html = new StringBuilder("<table><tr>");
        columns.forEach(c -> html.append(String.format("<th>%s</th>", c.getName())));
        html.append("</tr>");
        for (int i = 0; i < rows.size(); i++) {
            html.append("<tr>");
            for (Column column : columns) {
                html.append(String.format("<td>%s</td>", this.table.asList().get(i).get(column.getName().toLowerCase()).toString()));
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

}
