package org.openhab.binding.easyfamily.internal.json.data;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Operands {
    @SerializedName("MWRANGE")
    public List<MWRange> mwRange = new ArrayList<MWRange>();

    @SerializedName("NWRANGE")
    public List<NWRange> nwRange = new ArrayList<NWRange>();

    @SerializedName("IALL")
    public String iAll = "";

    @SerializedName("OALL")
    public String oAll = "";

    @SerializedName("AIALL")
    public String aIAll = "";

    @SerializedName("AOALL")
    public String aOAll = "";
}
