package com.bzdata.usermanagementsystem.service.Impl;

import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import com.bzdata.usermanagementsystem.model.UserPrincipal;
import com.bzdata.usermanagementsystem.repository.UserRepository;
import com.bzdata.usermanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.List;

import static com.bzdata.usermanagementsystem.constant.UserImplConstant.*;
import static com.bzdata.usermanagementsystem.enumeration.Role.ROLE_USER;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity=userRepository.findUserEntityByUsername(username);
        if(userEntity==null){
            log.error(NO_USER_FOUND_BY_USERNAME+ username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+ username);
        }else{
            userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
            userEntity.setLastLoginDate(new Date());
            userRepository.save(userEntity);
            UserPrincipal userPrincipal=new UserPrincipal(userEntity);
            log.info("Returning found user by username: "+ username);
            return userPrincipal;
        }
    }

    @Override
    public UserEntity register(String familyName, String remainingName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        UserEntity user = new UserEntity();
        user.setUserApplicationId(generateUserId());
        String password = generatePassword();
        user.setFamilyName(familyName);
        user.setRemainingName(remainingName);
        user.setFullName(familyName +' '+remainingName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword(password));
        user.setActive(true);
        user.setNonLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        log.info("New user password: " + password);
      //  emailService.sendNewPasswordEmail(firstName, password, email);
        return user;
    }
    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
    }
    private String encodePassword(String password) {

        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private UserEntity validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        UserEntity userByNewUsername = findUserByUsername(newUsername);
        UserEntity userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentUsername)) {
            UserEntity currentUser = findUserByUsername(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    @Override
    public List<UserEntity> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity findUserByUsername(String username) {
        return userRepository.findUserEntityByUsername(username);
    }

    @Override
    public UserEntity findUserByEmail(String email) {
        return userRepository.findUserEntityByEmail(email);
    }

    @Override
    public UserEntity addNewUser(String familyName, String remainingName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) {
        return null;
    }

    @Override
    public UserEntity updateUser(String currentUsername, String newFamilyName, String newRemainingName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) {
        return null;
    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public UserEntity updateProfileImage(String username, MultipartFile profileImage) {
        return null;
    }


}
