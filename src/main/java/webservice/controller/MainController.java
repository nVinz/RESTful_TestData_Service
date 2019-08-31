package webservice.controller;

import database.DataBase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.sql.ResultSet;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class MainController {

    @GetMapping(value = "/test")
    @ResponseBody
    public String testSys(){
        String result = "";

        DataBase db = new DataBase();
        db.connectToBD();
        Table res = db.executeQuery("select * from login");
        for (Row row : res.rows()) {
            for (Column column : res.columns()) {
                result += row.getString(column.getName());
            }
            result += "\n";
        }
        return result;
    }

    @RequestMapping(value = "/createTable{project}{tableName}{columns}{masks}", method = GET)
    @ResponseBody
    public String createTable(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("columns") String[] columns, @RequestParam("masks") String[] masks){
        String templateToInsert = "INSERT INTO masks VALUES %s";
        String templateToCreate = "CREATE TABLE %s (%s release varchar(255), blocked date, reused boolean)";
        String requestToInsert = "";
        String requestToCreate = "";
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String mask = masks[i];

            requestToCreate += column + " varchar(255), ";

            if (!requestToInsert.equals("")) requestToInsert += ", ";
            requestToInsert += String.format("('%s', '%s', '%s', '%s')", column, mask, tableName, project);

            System.out.println(column + " " + mask);
        }

        requestToInsert = String.format(templateToInsert, requestToInsert);
        requestToCreate = String.format(templateToCreate, tableName, requestToCreate);

        DataBase db = new DataBase();
        db.connectToBD();
        if (!db.isTableExists(tableName)) {
            db.executeQueryWithoutResult(requestToInsert);
            db.executeQueryWithoutResult(requestToCreate);
            return "Table was successfully created";
        }
        return String.format("Table with name %s is already existed", tableName);
    }
}
