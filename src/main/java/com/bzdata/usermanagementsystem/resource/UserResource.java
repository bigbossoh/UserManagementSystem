package com.bzdata.usermanagementsystem.resource;

import com.bzdata.usermanagementsystem.exception.ExceptionHandling;
import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path={"ums/api/v1/user","/"})
public class UserResource extends ExceptionHandling {
    @GetMapping("/home")
    public String showUser() throws EmailExistException {

        //return "application works fine";
        throw new EmailExistException("this email is already taken");
    }
}
