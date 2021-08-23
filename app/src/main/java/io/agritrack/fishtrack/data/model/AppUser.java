package io.agritrack.fishtrack.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

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
    public String registeredDate;

    @ColumnInfo(name = "active")
    public Boolean active;

    @ColumnInfo(name = "roles")
    public List<String> roles;

    @NonNull
    public String toString() {
        return username + " [" + email + "]";
    }
}
