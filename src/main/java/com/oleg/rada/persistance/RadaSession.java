package com.oleg.rada.persistance;

import com.oleg.rada.RomanToDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class RadaSession {

    @Id
    Integer id;
    String sName;
    String name;
    String sklikanya;
    String url;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    @OneToMany(mappedBy = "radaSession", fetch = FetchType.EAGER)
    List<RadaSessionDay> days = new ArrayList<RadaSessionDay>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSklikanya() {
        return sklikanya;
    }

    public void setSklikanya(String sklikanya) {
        this.sklikanya = sklikanya;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addDay(RadaSessionDay day){
        days.add(day);
    }

    public List<RadaSessionDay> getDays() {
        return days;
    }

    public void setDays(List<RadaSessionDay> days) {
        this.days = days;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void generateId() {
        int id = 0;
        for(int i=0; i<sName.length(); i++){
            try{
               id = id*10 + Integer.parseInt(sName.substring(i, i+1));
            }catch (Exception ex){
                break;
            }
        }
        String s = sklikanya.replace("скликання", "").trim();
        id = RomanToDecimal.romanToDecimal(s)*100 + id ;
        this.id = id;
    }
}
