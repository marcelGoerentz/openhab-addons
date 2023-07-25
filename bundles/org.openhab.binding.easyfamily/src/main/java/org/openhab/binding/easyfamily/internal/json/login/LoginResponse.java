package org.openhab.binding.easyfamily.internal.json.login;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("SYSINFO")
    public Sysinfo sysinfo = new Sysinfo();
    @SerializedName("SESSIONKEY")
    public String sessionKey = "";
}
