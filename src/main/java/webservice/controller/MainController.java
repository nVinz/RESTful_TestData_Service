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

    @GetMapping(value = "/s")
    @ResponseBody
    public String testSys1() {
        return "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"https://www.thymeleaf.org\" xmlns:sec=\"https://www.thymeleaf.org/thymeleaf-extras-springsecurity3\">\n" +
                "<head>\n" +
                "\t<title>TeReD</title>\n" +
                "\t<meta charset=\"UTF-8\">\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"css/styles.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"parentflex\">\n" +
                "\t<a class=\"borderlabel\">User > Project</a><br>\n" +
                "</div>\n" +
                "<div class=\"parentinline\">\n" +
                "\t<a class=\"buttonlist active\" disabled>Существующие ТД</a><br>\n" +
                "\t<a class=\"buttonlist\" href=\"createPage.html\">Создать ТД</a><br>\n" +
                "\t<a class=\"buttonlist\" href=\"importPage.html\">Импорт ТД</a><br>\n" +
                "\t<a class=\"buttonlist\" href=\"\">Создать REST-запрос</a><br>\n" +
                "</div>\n" +
                "<div class=\"parentflex\">\n" +
                "\t<a>\n" +
                "\t\t<a class=\"label\">Маска</a>\n" +
                "\t\t<input id=\"filterInput\" type=\"text\" class=\"input\">\n" +
                "        <button type=\"submit\" id=\"updateTable\">Обновить\n" +
                "        </button>\n" +
                "\t</a>\n" +
                "</div>\n" +
                "<div id=\"mainTable\" class=\"table\">\n" +
                "</div>\n" +
                "<script src=\"js/jquery.min.js\"></script>\n" +
                "<script src=\"js/mainPage.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }

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

        }
        else if (mask.contains("script")) {

        }
        else {

        }
        return true;
    }
}
