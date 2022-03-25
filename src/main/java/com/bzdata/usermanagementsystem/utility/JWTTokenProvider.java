package com.bzdata.usermanagementsystem.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import static com.bzdata.usermanagementsystem.constant.SecurityConstant.*;
import static java.util.Arrays.stream;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.bzdata.usermanagementsystem.model.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;
@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateJwtToken(UserPrincipal userPrincipal){
        String[] claims=getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(BZDATA_SARL)
                .withAudience(BZDATA_ADMINISTRATION)
                .withIssuedAt(new Date())
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES,claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }
    public List<GrantedAuthority> getAuthorities(String token){
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    }
    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken userPasswordAutToken=new
                UsernamePasswordAuthenticationToken(username,null,authorities);
        userPasswordAutToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAutToken;
    }
    public boolean isTokenValid(String username, String token){
        JWTVerifier verifier=getJWTVerifier();
        return StringUtils.isNoneEmpty(username)&& isTokenExpired(verifier,token);
    }

    public String getSubject(String token){
        JWTVerifier verifier=getJWTVerifier();
        return verifier.verify(token).getSubject();
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration=verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier =getJWTVerifier();
        return  verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try{
            Algorithm algorithm=Algorithm.HMAC512(secret);
            verifier=JWT.require(algorithm).withIssuer(BZDATA_SARL).build();
        }catch (JWTVerificationException exception){
           throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }


    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        List<String> authorities= new ArrayList<>();
        for(GrantedAuthority grantedAuthority: userPrincipal.getAuthorities()){
            authorities.add(grantedAuthority.getAuthority());
        }
        return authorities.toArray(new String[0]);

    }

}
