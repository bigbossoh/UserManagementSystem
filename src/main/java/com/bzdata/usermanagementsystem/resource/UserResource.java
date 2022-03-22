package com.bzdata.usermanagementsystem.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="ums/api/v1/user")
public class UserResource {
    @GetMapping("/home")
    public String showUser(){
        return "application works fine";
    }
}
