package com.bzdata.usermanagementsystem.resource;

import com.bzdata.usermanagementsystem.exception.ExceptionHandling;
import com.bzdata.usermanagementsystem.exception.model.EmailExistException;
import com.bzdata.usermanagementsystem.exception.model.UserNotFoundException;
import com.bzdata.usermanagementsystem.exception.model.UsernameExistException;
import com.bzdata.usermanagementsystem.model.UserEntity;
import com.bzdata.usermanagementsystem.model.UserPrincipal;
import com.bzdata.usermanagementsystem.service.UserService;
import com.bzdata.usermanagementsystem.utility.JWTTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.bzdata.usermanagementsystem.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;

@RestController
@AllArgsConstructor
@RequestMapping(path = {"ums/api/v1/user", "/"})
public class UserResource extends ExceptionHandling {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity userEntity) throws UserNotFoundException, EmailExistException, UsernameExistException {
        UserEntity newUser = userService.register(userEntity.getFamilyName(), userEntity.getRemainingName(),
                userEntity.getUsername(), userEntity.getEmail());
        return new ResponseEntity<>(newUser, OK);
    }
    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        authenticate(user.getUsername(), user.getPassword());
        UserEntity loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
