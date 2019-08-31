package generations;

import nl.flotsam.xeger.Xeger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

public class Generating {

    public static String generateByRegEXP(String regex) {
        return new Xeger(regex).generate();
    }

    public static String generateCharSet(){
        return generateCharSet(0, 255);
    }

    public static String generateCharSet(int min, int max) {
        String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder();
        int len = min + (int) (Math.random() * (max - min + 1));
        for (int i = 0; i < len; i++) {
            int index = (int) (symbols.length() * Math.random());
            sb.append(symbols.charAt(index));
        }

        return sb.toString();
    }

    public static int generateNumeric(){
        return generateNumeric(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int generateNumeric(int min, int max){
        return (int) (min + (long) (Math.random() * (max - min + 1)));
    }

    public static boolean generateBool(){
        return (int) (Math.random() * 2) == 0;
    }

    public static Date randomDate() {

        GregorianCalendar gc = new GregorianCalendar();

        int year = generateNumeric(1900, 2100);

        gc.set(gc.YEAR, year);

        int dayOfYear = generateNumeric(1, gc.getActualMaximum(gc.DAY_OF_YEAR));

        gc.set(gc.DAY_OF_YEAR, dayOfYear);
        String date = gc.get(gc.DAY_OF_MONTH) + "-" + (gc.get(gc.MONTH) + 1) + "-" + gc.get(gc.YEAR);
        try {
            return new SimpleDateFormat("dd-MM-yyyy").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
