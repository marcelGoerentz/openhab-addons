package org.openhab.binding.easyfamily.internal.json.data;

import com.google.gson.annotations.SerializedName;

public class MWRange {
    @SerializedName("START")
    public int start = 0;

    @SerializedName("END")
    public int end = 0;

    @SerializedName("V")
    public String v = "";
}
