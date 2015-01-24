package com.oleg.rada.persistance.dao;

import com.oleg.rada.persistance.Law;

import java.util.List;

/**
 * User: okostyuk
 * Date: 9/8/2014
 */
public interface LawDAO {
    void save(Law law);
    void saveAll(Iterable<Law> laws);
}
