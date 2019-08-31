package webservice.controller;

import database.DataBase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
}
