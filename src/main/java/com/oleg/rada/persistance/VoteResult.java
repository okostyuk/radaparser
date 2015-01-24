package com.oleg.rada.persistance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class VoteResult {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Vote vote;

    @ManyToOne
    private MP mp;

    private String res;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
    }

    public MP getMp() {
        return mp;
    }

    public void setMp(MP mp) {
        this.mp = mp;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }
}
