package com.bzdata.usermanagementsystem.service;

import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    UserEntity register(String familyName, String remainingName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException;
    List<UserEntity> getUsers();

    UserEntity findUserByUsername(String username);

    UserEntity findUserByEmail(String email);

    UserEntity addNewUser(String familyName, String remainingName, String username, String email, String role,
                          boolean isNonLocked, boolean isActive, MultipartFile profileImage);

    UserEntity updateUser(String currentUsername, String newFamilyName, String newRemainingName, String newUsername,
                          String newEmail,String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage);

    void deleteUser(String username);

    void resetPassword(String email);

    UserEntity updateProfileImage(String username, MultipartFile profileImage);
}
