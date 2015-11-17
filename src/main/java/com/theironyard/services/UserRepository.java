package com.theironyard.services;

import com.theironyard.entities.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by jessicahuffstutler on 11/17/15.
 */
public interface UserRepository extends CrudRepository<User, Integer> {
    User findOneByUsername(String username); //custom method to find by username instead of id
}
