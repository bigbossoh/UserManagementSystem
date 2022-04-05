package com.bzdata.usermanagementsystem.resource;

import com.bzdata.usermanagementsystem.exception.ExceptionHandling;
import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import com.bzdata.usermanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@RestController
@AllArgsConstructor
@RequestMapping(path = {"ums/api/v1/user", "/"})
public class UserResource extends ExceptionHandling {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity userEntity) throws UserNotFoundException, EmailExistException, UsernameExistException {
        UserEntity newUser = userService.register(userEntity.getFamilyName(), userEntity.getRemainingName(),
                userEntity.getUsername(), userEntity.getEmail());
        return new ResponseEntity<>(newUser, OK);

    }
}
