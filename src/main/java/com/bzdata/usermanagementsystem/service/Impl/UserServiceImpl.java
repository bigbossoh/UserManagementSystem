package com.bzdata.usermanagementsystem.service.Impl;

import com.bzdata.usermanagementsystem.enumeration.Role;
import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.EmailNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import com.bzdata.usermanagementsystem.model.UserPrincipal;
import com.bzdata.usermanagementsystem.repository.UserRepository;
import com.bzdata.usermanagementsystem.service.EmailService;
import com.bzdata.usermanagementsystem.service.LoginAttemptService;
import com.bzdata.usermanagementsystem.service.UserService;
import javafx.beans.property.adapter.JavaBeanDoublePropertyBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.bzdata.usermanagementsystem.constant.FileConstant.*;
import static com.bzdata.usermanagementsystem.constant.UserImplConstant.*;
import static com.bzdata.usermanagementsystem.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.*;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity=userRepository.findUserEntityByUsername(username);
        if(userEntity==null){
            log.error(NO_USER_FOUND_BY_USERNAME+ username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+ username);
        }else{
            validateLoginAttempt(userEntity);
            userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
            userEntity.setLastLoginDate(new Date());
            userRepository.save(userEntity);
            UserPrincipal userPrincipal=new UserPrincipal(userEntity);
            log.info("Returning found user by username: "+ username);
            return userPrincipal;
        }
    }
    private void validateLoginAttempt(UserEntity user) {
        if(user.isNonLocked()) {
            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNonLocked(false);
            } else {
                user.setNonLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public UserEntity register(String familyName, String remainingName, String username, String email) throws
            UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
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
        emailService.sendNewPasswordEmail(familyName, password, email);
        return user;
    }
    @Override
    public UserEntity addNewUser(String familyName, String remainingName, String username, String email,
                                 String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        UserEntity user = new UserEntity();
        String password = generatePassword();
        user.setUserApplicationId(generateUserId());
        user.setFamilyName(familyName);
        user.setRemainingName(remainingName);
        user.setFullName(familyName +" "+remainingName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNonLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        saveProfileImage(user, profileImage);
        log.info("New user password: " + password);
        return user;
    }

    private void saveProfileImage(UserEntity user, MultipartFile profileImage) throws IOException
//            , NotAnImageFileException
    {
        if (profileImage != null) {
//            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
//                throw new NotAnImageFileException(profileImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
//            }
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                log.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername()
                    + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            log.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
                + username + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH+username).toUriString();
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
    public UserEntity updateUser(String currentUsername, String newFamilyName, String newRemainingName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
        UserEntity currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        currentUser.setFamilyName(newFamilyName);
        currentUser.setRemainingName(newRemainingName);
        currentUser.setFullName(newFamilyName+" "+newRemainingName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNonLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(String username) throws IOException {
        UserEntity user = userRepository.findUserEntityByUsername(username);
        Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        userRepository.deleteById(user.getId());
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        UserEntity user =userRepository. findUserEntityByEmail(email);
        if(user==null){
            throw new EmailNotFoundException(NO_USER_FOUND_BY_USERNAME+email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFamilyName(),password,user.getEmail());

    }

    @Override
    public UserEntity updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
        UserEntity user= validateNewUsernameAndEmail(username,null,null);
        saveProfileImage(user,profileImage);
        return null;
    }


}
