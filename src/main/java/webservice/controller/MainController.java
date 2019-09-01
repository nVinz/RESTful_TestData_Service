package webservice.controller;

import database.DataBase;
import generations.Generating;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;

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

    @RequestMapping(value = "/createTable{project}{tableName}{description}{columns}{masks}", method = GET)
    @ResponseBody
    public String createTable(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("description") String description, @RequestParam("columns") String[] columns, @RequestParam("masks") String[] masks){
        String templateToInsertMasks = "INSERT INTO masks(\"column\", \"mask\", \"id\") VALUES %s";
        String templateToInsertTables = "INSERT INTO tables(\"id\", \"name\", \"project\", \"description\") VALUES (coalesce((SELECT \"id\"+1 FROM tables ORDER BY \"id\" DESC LIMIT 1), 1), '%s', '%s', '%s') RETURNING \"id\"";
        String templateToCreate = "CREATE TABLE %s (%s release varchar(255), blocked date, reused boolean)";
        String requestToInsertMasks = "";
        String requestToInsertTables = "";
        String requestToCreate = "";
        String id;

        requestToInsertTables = String.format(templateToInsertTables, tableName, project, description);

        DataBase db = new DataBase();
        db.connectToBD();
        if (!db.isTableExists(tableName)) {
            try {
                Table res = db.executeQuery(requestToInsertTables);
                id = res.rows().get(0).getString(0);
            }
            catch (PSQLException e) {
                System.out.println("Error occured while request " + e.getMessage());
                return "Error occured while request " + e.getMessage();
            }
        }
        else return String.format("Table with name %s has already existed", tableName);

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i].toLowerCase();
            String mask = masks[i];

            requestToCreate += column + " varchar(255), ";

            if (!requestToInsertMasks.equals("")) requestToInsertMasks += ", ";
            requestToInsertMasks += String.format("('%s', '%s', '%s')", column, mask, id);
        }

        requestToInsertMasks = String.format(templateToInsertMasks, requestToInsertMasks);
        requestToCreate = String.format(templateToCreate, tableName, requestToCreate);

        try {
            db.executeQueryWithoutResult(requestToInsertMasks);
            db.executeQueryWithoutResult(requestToCreate);
        }
        catch (PSQLException e) {
            System.out.println("Error occured while request " + e.getMessage());
            return "Error occured while request " + e.getMessage();
        }

        return "Table was successfully created";
    }

    @RequestMapping(value = "/insertValues{project}{tableName}{columns}{values}", method = GET)
    @ResponseBody
    public String insertValues(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("columns") String[] columns, @RequestParam("values") String values){
        String templateToGetMasks = "SELECT \"column\", \"mask\" FROM masks JOIN tables ON masks.\"id\" = tables.\"id\" WHERE \"name\" = '%s' AND \"project\" = '%s' AND \"column\" in (%s)";
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

        requestToGetMasks = String.format(templateToGetMasks, tableName, project, requestColumns.replace("\"", "'"));

        DataBase db = new DataBase();
        db.connectToBD();
        if (db.isTableExists(tableName)) {
            try{
                res = db.executeQuery(requestToGetMasks);
            }
            catch (PSQLException e){
                System.out.println("Error occured while request " + e.getMessage());
                return "Error occured while request " + e.getMessage();
            }
        }
        else return String.format("Table with name %s doesn't exist", tableName);

        for (int i = 0; i < splittedValues.length; i++) {
            String[] splittedRow = splittedValues[i].split(",");
            if (!requestToInsert.equals("")) requestToInsert += "), (";
            for (int j = 0; j < splittedRow.length; j++) {
                if (j != 0) requestToInsert += ", ";
                String mask = getMaskByColumnName(columns[j], res);
                if (!mask.equals("")) {
                    if (!isValueValidByMask(splittedRow[j], mask)) return String.format("Value %s isn't valid by mask %s", splittedRow[j], mask);
                }
                requestToInsert += String.format("'%s'", splittedRow[j]);
            }
        }

        requestToInsert = String.format(templateToInsert, tableName, requestColumns, requestToInsert);

        try{
            db.executeQueryWithoutResult(requestToInsert);
        }
        catch (PSQLException e){
            System.out.println("Error occured while request " + e.getMessage());
            return "Error occured while request " + e.getMessage();
        }
        return "Values was successfully inserted";
    }

    private String getMaskByColumnName(String columnName, Table table) {
        for (Row row : table.rows()) {
            if (row.getString("column").equals(columnName.toLowerCase())) return row.getString("mask");
        }
        System.out.println("No column found");
        return "";
    }

    private boolean isValueValidByMask(String value, String mask) {
        if (mask.contains("regexp")){
            return value.matches(mask.replace("regexp", ""));
        }
        else if (mask.contains("script")) {
            return true;
        }
        else {
            if (mask.contains("chr")) return value.matches("\\w+");
            if (mask.contains("int")) return value.matches("\\d+");
            if (mask.contains("bool")) return value.matches("(true)|(false)");
            if (mask.contains("date")) return value.matches("\\d{2}\\.\\d{2}.\\d{4}");
            return true;
        }
    }

    @RequestMapping(value = "/generateValues{project}{tableName}{count}", method = GET, produces = "application/json")
    @ResponseBody
    public Collection generateValues(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("count") int count){
        String templateToGetMasks = "SELECT \"column\", \"mask\" FROM masks JOIN tables ON masks.\"id\" = tables.\"id\" WHERE \"name\" = '%s' AND \"project\" = '%s' AND \"column\" in (SELECT column_name FROM information_schema.columns WHERE table_name = '%s')";
        String templateToInsert = "INSERT INTO %s(%s) VALUES (%s)";
        String requestToInsert = "";
        String requestColumns = "";
        String requestToGetMasks = "";
        Table res = null;

        requestToGetMasks = String.format(templateToGetMasks, tableName, project, tableName.toLowerCase());

        DataBase db = new DataBase();
        db.connectToBD();
        if (db.isTableExists(tableName)) {
            try{
                res = db.executeQuery(requestToGetMasks);
                for (int i = 0; i < res.rows().size(); i++) {
                    if (i != 0) requestColumns += ", ";
                    requestColumns += String.format("\"%s\"", res.rows().get(i).getString("column").toLowerCase());
                }
            }
            catch (PSQLException e){
                System.out.println("Error occured while request " + e.getMessage());
                return Collections.singleton("Error occured while request " + e.getMessage());
            }
        }
        else return Collections.singleton(String.format("Table with name %s doesn't exist", tableName));

        for (int i = 0; i < count; i++) {
            if (!requestToInsert.equals("")) requestToInsert += "), (";
            for (int j = 0; j < res.rows().size(); j++) {
                if (j != 0) requestToInsert += ", ";
                String mask = res.rows().get(j).getString("mask");
                String value = generateValueByMask(mask);

                requestToInsert += String.format("'%s'", value);
            }
        }

        Collection col = Collections.singleton(requestToInsert.split("\\), \\("));

        requestToInsert = String.format(templateToInsert, tableName, requestColumns, requestToInsert);

        try{
            db.executeQueryWithoutResult(requestToInsert);
        }
        catch (PSQLException e){
            System.out.println("Error occured while request " + e.getMessage());

            return Collections.singleton("Error occured while request " + e.getMessage());
        }
        return col;
    }

    private String generateValueByMask(String mask){
        if (mask.contains("regexp")){
            return Generating.generateByRegEXP(mask.replace("regexp", ""));
        }
        else if (mask.contains("script")) {
            return "";
        }
        else {
            if (mask.contains("chr")) return Generating.generateCharSet();
            if (mask.contains("int")) return String.valueOf(Generating.generateNumeric());
            if (mask.contains("bool")) return String.valueOf(Generating.generateBool());
            if (mask.contains("date")) return String.valueOf(Generating.randomDate());
            return "";
        }
    }

    @RequestMapping(value = "/getValues{project}{tableName}{count}", method = GET, produces = "application/json")
    @ResponseBody
    public Collection getValues(@RequestParam("project") String project, @RequestParam("tableName") String tableName, @RequestParam("count") int count){
        String templateToGetValues = "SELECT * FROM %s WHERE \"blocked\" IS NULL OR \"blocked\" > NOW() LIMIT %s";
        String templateToUpdate = "";
        String requestToGetValues = "";
        String requestToUpdate = "";

        Table res = null;

        requestToGetValues = String.format(templateToGetValues, tableName, count);

        DataBase db = new DataBase();
        db.connectToBD();
        if (db.isTableExists(tableName)) {
            try{
                res = db.executeQuery(requestToGetValues);
            }
            catch (PSQLException e){
                System.out.println("Error occured while request " + e.getMessage());
                return Collections.singleton("Error occured while request " + e.getMessage());
            }
        }
        else return Collections.singleton(String.format("Table with name %s doesn't exist", tableName));

        String rows[][] = new String[res.rows().size()][];
        for (int i = 0; i < res.rows().size(); i++) {
            String values[] = new String[res.columns().size()];
            for (int j = 0; j < res.columns().size(); j++) {
                String value = res.rows().get(i).getString(j);
                values[j] = value;
            }
            rows[i] = values;
        }

//        try{
//            db.executeQueryWithoutResult(requestToInsert);
//        }
//        catch (PSQLException e){
//            System.out.println("Error occured while request " + e.getMessage());
//
//            return Collections.singleton("Error occured while request " + e.getMessage());
//        }
        return Collections.singleton(rows);
    }
}
