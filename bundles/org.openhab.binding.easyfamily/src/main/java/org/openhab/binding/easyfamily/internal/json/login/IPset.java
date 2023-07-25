package org.openhab.binding.easyfamily.internal.json.login;

import com.google.gson.annotations.SerializedName;

public class IPset {
    @SerializedName("ACTIP")
    public String actIP = "";

    @SerializedName("IPMODE")
    public String ipMode = "";

    @SerializedName("ACTMASK")
    public String actMask = "";

    @SerializedName("ACTGTW")
    public String actGtw = "";
}
