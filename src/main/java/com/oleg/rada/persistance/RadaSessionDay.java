package com.oleg.rada.persistance;


import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity
public class RadaSessionDay {

    @Id
    Integer id;

    String month;
    Integer day;
    String type;
    String url;

    @ManyToOne
    RadaSession radaSession;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Law> laws;

    public List<Law> getLaws() {
        return laws;
    }

    public void setLaws(List<Law> laws) {
        this.laws = laws;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RadaSession getRadaSession() {
        return radaSession;
    }

    public void setRadaSession(RadaSession radaSession) {
        this.radaSession = radaSession;
    }


    public void generateId(){
        int month = monthToInt(getMonth().trim());
        if (month == 0)
            System.err.println(getMonth());

        int id = day + month*100 + radaSession.getId() * 10000;
        this.id = id;
    }

    private static int monthToInt(String month){
        if (month.equals("Січень")) {
            return 1;
        }else if (month.equals("Лютий")) {
            return 2;
        }else if (month.equals("Березень")) {
            return 3;
        }else if (month.equals("Квітень")) {
            return 4;
        }else if (month.equals("Травень")) {
            return 5;
        }else if (month.equals("Червень")) {
            return 6;
        }else if (month.equals("Липень")) {
            return 7;
        }else if (month.equals("Серпень")) {
            return 8;
        }else if (month.equals("Вересень")) {
            return 9;
        }else if (month.equals("Жовтень")) {
            return 10;
        }else if (month.equals("Листопад")) {
            return 11;
        }else if (month.equals("Грудень")) {
            return 12;
        }
        return 0;
    }

    @Override
    public String toString() {
        return day+"/"+month;
    }
}
