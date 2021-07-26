package io.agritrack.fishtrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "appUser")
public class AppUser {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "legal_name")
    public String legalName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "pin")
    public String pin;

    @ColumnInfo(name = "registered_date")
    public Date registeredDate;

    @ColumnInfo(name = "active")
    public Boolean active;

    @NonNull
    public String toString() {
        return username + " [" + email + "]" ;
    }
}
