package webservice.controller;

import database.DataBase;
import org.postgresql.util.PSQLException;
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
        Table res = null;

        DataBase db = new DataBase();
        db.connectToBD();
        try {
            res = db.executeQuery("select * from login");
        }
        catch (PSQLException e) {
            System.out.println("Error occured while request " + e.getMessage());
            return "Error occured while request " + e.getMessage();
        }
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
            String column = columns[i].toLowerCase();
            String mask = masks[i];

            requestToCreate += column + " varchar(255), ";

            if (!requestToInsert.equals("")) requestToInsert += ", ";
            requestToInsert += String.format("('%s', '%s', '%s', '%s')", column, mask, tableName, project);
        }

        requestToInsert = String.format(templateToInsert, requestToInsert);
        requestToCreate = String.format(templateToCreate, tableName, requestToCreate);

        DataBase db = new DataBase();
        db.connectToBD();
        if (!db.isTableExists(tableName)) {
            try {
                db.executeQueryWithoutResult(requestToInsert);
                db.executeQueryWithoutResult(requestToCreate);
            }
            catch (PSQLException e) {
                System.out.println("Error occured while request " + e.getMessage());
                return "Error occured while request " + e.getMessage();
            }

            return "Table was successfully created";
        }
        return String.format("Table with name %s has already existed", tableName);
    }

    @RequestMapping(value = "/insertValues{project}{tableName}{columns}{values}", method = GET)
    @ResponseBody
    public String insertValues(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("columns") String[] columns, @RequestParam("values") String values){
        String templateToGetMasks = "SELECT column, mask FROM masks WHERE table = '%s' AND project = '%s' AND column in (%s)";
        String templateToInsert = "INSERT INTO %s(%s) VALUES (%s)";
        String requestToInsert = "";
        String requestColumns = "";
        String requestToGetMasks = "";
        Table res = null;

        String[] splittedValues = values.split(";");
        for (int i = 0; i < columns.length; i++) {
            if (i != 0) requestColumns += ", ";
            requestColumns += String.format("\"%s\"", columns[i].toLowerCase());
        }

        for (int i = 0; i < splittedValues.length; i++) {
            String[] splittedRow = splittedValues[i].split(",");
            if (!requestToInsert.equals("")) requestToInsert += "), (";
            for (int j = 0; j < splittedRow.length; j++) {
                if (j != 0) requestToInsert += ", ";
                requestToInsert += String.format("'%s'", splittedRow[j]);
            }
        }

        requestToInsert = String.format(templateToInsert, tableName, requestColumns, requestToInsert);
        requestToGetMasks = String.format(templateToGetMasks, tableName, project, requestColumns);

        DataBase db = new DataBase();
        db.connectToBD();
        if (db.isTableExists(tableName)) {
            try{
                res = db.executeQuery(requestToGetMasks);
                db.executeQueryWithoutResult(requestToInsert);
            }
            catch (PSQLException e){
                System.out.println("Error occured while request " + e.getMessage());
                return "Error occured while request " + e.getMessage();
            }
            return "Values was successfully inserted";
        }
        return String.format("Table with name %s doesn't exist", tableName);
    }
}
