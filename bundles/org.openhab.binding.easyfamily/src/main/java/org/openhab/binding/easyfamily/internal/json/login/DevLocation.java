package org.openhab.binding.easyfamily.internal.json.login;

import com.google.gson.annotations.SerializedName;

public class DevLocation {
    @SerializedName("LONGITUDE")
    public String longitude = "";

    @SerializedName("LATITUDE")
    public String latitude = "";
}
