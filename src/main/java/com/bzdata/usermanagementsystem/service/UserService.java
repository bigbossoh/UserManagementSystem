package com.bzdata.usermanagementsystem.service;

import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.EmailNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    UserEntity register(String familyName, String remainingName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException;

    List<UserEntity> getUsers();

    UserEntity findUserByUsername(String username);

    UserEntity findUserByEmail(String email);

    UserEntity addNewUser(String familyName, String remainingName, String username, String email, String role,
                          boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

    UserEntity updateUser(String currentUsername, String newFamilyName, String newRemainingName, String newUsername,
                          String newEmail,String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

    void deleteUser(String username) throws IOException;

    void resetPassword(String email) throws EmailNotFoundException, MessagingException;

    UserEntity updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;
}
