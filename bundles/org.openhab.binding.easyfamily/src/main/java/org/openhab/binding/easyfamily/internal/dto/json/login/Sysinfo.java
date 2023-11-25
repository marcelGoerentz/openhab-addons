/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.easyfamily.internal.dto.json.login;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Sysinfo} is for desirializing the SYSINFO from json
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Sysinfo {
    @SerializedName("STATE")
    public String state = "";

    @SerializedName("EXTSTATE")
    public Extstate extState = new Extstate();

    @SerializedName("DATE")
    public String date = "";

    @SerializedName("TIME")
    public String time = "";

    @SerializedName("MAC")
    public String mac = "";

    @SerializedName("IPSET")
    public IPset ipSet = new IPset();

    @SerializedName("VERS")
    public String vers = "";

    @SerializedName("BUILD")
    public String build = "";

    @SerializedName("DEVNAME")
    public String devName = "";

    @SerializedName("TYPE")
    public String type = "";

    @SerializedName("DEVLOCATION")
    public DevLocation devLocation = new DevLocation();

    @SerializedName("NETID")
    public int netID = 0;

    @SerializedName("ADMIN")
    public boolean admin = false;

    @SerializedName("DNS")
    public String dns = "";

    @SerializedName("ETHERNETDOMAIN")
    public String ethernetDomain = "";

    @SerializedName("EASYNETINFO")
    public EasyNetInfo easyNetInfo = new EasyNetInfo();

    @SerializedName("PROGNAME")
    public String progname = "";

    @SerializedName("BTLVERSION")
    public String btlVersion = "";

    @SerializedName("SERIALNUMBER")
    public String serialNumber = "";

    @SerializedName("DATEOFMANUFACTURE")
    public String dateOfManufacture = "";

    @SerializedName("DEVICEIO")
    public String deviceIO = "";

    @SerializedName("WEBSERPORT")
    public int webSerPort = 0;

    @SerializedName("ENABLEIOREAD")
    public int enableIORead = 0;

    @SerializedName("ALLOWANANYMOUS")
    public int allowAnonymous = 0;

    @SerializedName("PARAMLIST")
    public int paramList = 0;

    @SerializedName("ACCESS_CLOCK")
    public int accessClock = 0;

    @SerializedName("ACCESS_MODE")
    public int accessMode = 0;

    @SerializedName("ACCESS_PARAM")
    public int accessParam = 0;

    @SerializedName("ACCESS_EMAIL")
    public int accessEmail = 0;

    @SerializedName("ACCESS_KEYS")
    public int accessKeys = 0;

    @SerializedName("MACCESS")
    public int[] mAccess = { 0, 0 };

    @SerializedName("NACCESS")
    public int[] nAccess = { 0, 0 };
}
