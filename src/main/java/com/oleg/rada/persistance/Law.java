package com.oleg.rada.persistance;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Oleg on 07.09.2014.
 */
@Entity
public class Law {

    public Law() {}

    @Id
    private Integer id;

    private String num;
    private String date;
    @Column(columnDefinition = "VARCHAR(2000)")
    private String name;
    private String link;
    private String voteResultLink;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<Vote> votes;

    public String getVoteResultLink() {
        return voteResultLink;
    }

    public void setVoteResultLink(String voteResultLink) {
        this.voteResultLink = voteResultLink;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
        if (num.hashCode() == 0){
            int i = 4;
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }
}
