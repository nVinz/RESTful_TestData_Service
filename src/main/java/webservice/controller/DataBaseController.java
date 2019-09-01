package webservice.controller;

import database.DBReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class DataBaseController {

    // Метод возвращаем HTML таблицу (в тегах <table>) по имени таблицы
    @RequestMapping(value = "/getHTMLTable{tableName}{tableStyle}", method = GET)
    @ResponseBody
    public String createTable(@RequestParam("tableName") String tableName,
                              @RequestParam("tableStyle") String tableStyle) {
        DBReader db = new DBReader();
        return db.getHTMLTable(tableName, "*", "");
    }

    // Метод возвращаем HTML таблицу (в тегах <table>) по имени таблицы с фильтром по колонке
    @RequestMapping(value = "/getFilteredHTMLTable{tableName}{columnName}{filter}{tableStyle}", method = GET)
    @ResponseBody
    public String createTable(@RequestParam("tableName") String tableName,
                              @RequestParam("columnName") String columnName,
                              @RequestParam("filter") String filter) {
        DBReader db = new DBReader();
        return db.getFilteredHTMLTable(tableName, "*", columnName, filter, "");
    }


}
