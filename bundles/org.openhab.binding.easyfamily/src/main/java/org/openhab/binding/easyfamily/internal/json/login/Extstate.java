package org.openhab.binding.easyfamily.internal.json.login;

import com.google.gson.annotations.SerializedName;

public class Extstate {
    @SerializedName("EXTDATA")
    public String extData = "";

    @SerializedName("EXTCFG")
    public String extCfg = "";

    @SerializedName("EXTBUS")
    public String extBus = "";

    @SerializedName("EXTCYC")
    public String extCyc = "";
}
