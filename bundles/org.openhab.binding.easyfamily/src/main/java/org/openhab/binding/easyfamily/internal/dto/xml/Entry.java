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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link Entry} class manages .
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Entry {

    @XStreamAsAttribute
    private final String adr;

    @XStreamAlias("i")
    private final Ich ich;

    @XStreamAlias("r")
    private final ConfigData config;

    @XStreamAlias("p")
    private PData pData;

    /**
     * @return the pData
     */
    public PData getpData() {
        return pData;
    }

    /**
     * @param pData the pData to set
     */
    public void setpData(PData pData) {
        this.pData = pData;
    }

    @XStreamImplicit
    private List<Comment> commentsList;

    public Entry(String adr, Ich ich, ConfigData config, List<Comment> commentsList, PData pData) {
        this.adr = adr;
        this.ich = ich;
        this.config = config;
        this.commentsList = commentsList;
        this.pData = pData;
    }

    public void setCommentsList(List<Comment> commentsList) {
        this.commentsList = commentsList;
    }

    public void addComment(Comment comment) {
        commentsList.add(comment);
    }

    /**
     * @return the adr
     */
    public String getAdr() {
        return adr;
    }

    /**
     * @return the ich
     */
    public Ich getIch() {
        return ich;
    }

    /**
     * @return the config
     */
    public ConfigData getConfig() {
        return config;
    }

    /**
     * @return the commentsList
     */
    public List<Comment> getCommentsList() {
        return commentsList;
    }
}
