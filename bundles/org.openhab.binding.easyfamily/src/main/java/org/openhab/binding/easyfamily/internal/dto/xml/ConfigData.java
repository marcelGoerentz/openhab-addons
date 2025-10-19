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
package org.openhab.binding.easyfamily.internal.dto.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("r")
public class ConfigData {
    @XStreamAsAttribute
    private String n;

    @XStreamAsAttribute
    private String v;

    @XStreamImplicit(itemFieldName = "cm")
    private List<Comment> cm;

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public List<Comment> getCm() {
        return cm;
    }

    public void setCm(List<Comment> cm) {
        this.cm = cm;
    }
}
