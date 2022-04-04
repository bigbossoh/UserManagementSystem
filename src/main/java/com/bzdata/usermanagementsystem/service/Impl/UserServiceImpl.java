package com.bzdata.usermanagementsystem.service.Impl;

import com.bzdata.usermanagementsystem.model.UserEntity;
import com.bzdata.usermanagementsystem.model.UserPrincipal;
import com.bzdata.usermanagementsystem.repository.UserRepository;
import com.bzdata.usermanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity=userRepository.findUserEntityByUsername(username);
        if(userEntity==null){
            log.error("User not found by username: "+ username);
            throw new UsernameNotFoundException("User not found by username: "+ username);
        }else{
            userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
            userEntity.setLastLoginDate(new Date());
            userRepository.save(userEntity);
            UserPrincipal userPrincipal=new UserPrincipal(userEntity);
            log.info("Returning found user by username: "+ username);
            return userPrincipal;
        }
    }
}
