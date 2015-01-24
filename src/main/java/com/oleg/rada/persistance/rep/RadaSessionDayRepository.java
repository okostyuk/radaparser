package com.oleg.rada.persistance.rep;

import com.oleg.rada.persistance.RadaSessionDay;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RadaSessionDayRepository extends CrudRepository<RadaSessionDay, Integer> {
    @Query(nativeQuery = true, value = "select * from radasessionday where url is not null")
    List<RadaSessionDay> findWithUrl();
}
