package org.openhab.binding.easyfamily.internal.json.data;

import org.openhab.binding.easyfamily.internal.json.login.Sysinfo;

import com.google.gson.annotations.SerializedName;

public class DataResponse {
    @SerializedName("SYSINFO")
    public Sysinfo sysinfo = new Sysinfo();

    @SerializedName("OPERANDS")
    public Operands operands = new Operands();

    public DataResponse(DataResponse dataResponse) {
        this.sysinfo = dataResponse.sysinfo;
        this.operands = dataResponse.operands;
    }
}
