package com.oleg.rada.persistance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class MP {

    @Id
    @GeneratedValue
    private Integer id;
    private String fio;
    private Integer convening;
    private String fraction;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public Integer getConvening() {
        return convening;
    }

    public void setConvening(Integer convening) {
        this.convening = convening;
    }

    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }
}
