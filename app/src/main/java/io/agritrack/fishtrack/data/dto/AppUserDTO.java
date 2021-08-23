package io.agritrack.fishtrack.data.dto;

import java.util.Date;
import java.util.List;

import io.agritrack.fishtrack.data.model.AppUser;

public class AppUserDTO {

    public Long id;
    public String full_name;
    public String email;
    public String phone;
    public String user_name;
    public String pin;
    public String registration_tstamp;
    public String last_login_tstamp;
    public Boolean active;
    public List<String> roles;

    public static AppUser convert(AppUserDTO appUserDTO) {
        AppUser appUser = new AppUser();
        appUser.id = appUserDTO.id;
        appUser.legalName = appUserDTO.full_name;
        appUser.email = appUserDTO.email;
        appUser.phone = appUserDTO.phone;
        appUser.username = appUserDTO.user_name;
        appUser.pin = appUserDTO.pin;
        appUser.registeredDate = appUserDTO.registration_tstamp;
        appUser.active = appUserDTO.active;
        appUser.roles = appUserDTO.roles;
        return appUser;
    }
}
