package com.bzdata.usermanagementsystem.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Utilisateur")
public class UserEntity extends AbstractEntity  {
    String userApplicationId;
    String familyName;
    String remainingName;
    String fullName;
    String username;
    String password;
    String email;
    String profileImageUrl;
    Date lastLoginDate;
    Date lastLoginDateDisplay;
    Date joinDate;
    String role;
    String[] authorities;
    boolean isActive;
    boolean isNonLocked;


}
