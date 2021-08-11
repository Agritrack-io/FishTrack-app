package io.agritrack.fishtrack.data.dto;

import java.util.Date;
import java.util.List;

import io.agritrack.fishtrack.data.model.AppUser;

public class AppUserDTO {

    public Long id;
    public String legalName;
    public String email;
    public String phone;
    public String username;
    public String pin;
    public Date registeredDate;
    public Boolean active;
    public List<String> roles;

    public static AppUser convert(AppUserDTO appUserDTO) {
        AppUser appUser = new AppUser();
        appUser.id = appUserDTO.id;
        appUser.legalName = appUserDTO.legalName;
        appUser.email = appUserDTO.email;
        appUser.phone = appUserDTO.phone;
        appUser.username = appUserDTO.username;
        appUser.pin = appUserDTO.pin;
        appUser.registeredDate = appUserDTO.registeredDate;
        appUser.active = appUserDTO.active;
        appUser.roles = appUserDTO.roles;
        return appUser;
    }
}
