package com.oleg.rada.persistance.rep;

import com.oleg.rada.persistance.Law;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;


public interface LawRepository extends CrudRepository<Law, Integer> {

}
