package io.agritrack.kefalonia.data.dto;

import com.google.gson.annotations.SerializedName;

public class DbConfigDTO {
    @SerializedName("host")
    public String host;

    @SerializedName("port")
    public Long port;

    @SerializedName("login")
    public String login;

    @SerializedName("pwd")
    public String pwd;

    @SerializedName("dbase")
    public String dbase;

    @SerializedName("driver")
    public String driver;
}
