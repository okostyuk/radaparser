package com.oleg.rada.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class Vote {

    public Vote() {}

    @Id
    private Integer id;
    @ManyToOne
    private Law law;
    private Date date;
    @Column(columnDefinition = "LONGTEXT")
    private String name;
    private String resText;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResText() {
        return resText;
    }

    public void setResText(String resText) {
        this.resText = resText;
    }

    public Law getLaw() {
        return law;
    }

    public void setLaw(Law law) {
        this.law = law;
    }
}
