package com.oleg.rada.persistance.rep;

import com.oleg.rada.persistance.Law;
import com.oleg.rada.persistance.Vote;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VoteRepository extends CrudRepository<Vote, Integer> {
    List<Vote> findByLaw(Law law);
}
