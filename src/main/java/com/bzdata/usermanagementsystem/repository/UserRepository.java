package com.bzdata.usermanagementsystem.repository;

import com.bzdata.usermanagementsystem.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findUserEntityByUsername(String username);
    UserEntity findUserEntityByEmail(String email);
}
