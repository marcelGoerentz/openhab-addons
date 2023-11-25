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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link XNFO} class manages .
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class XNFO {

    @XStreamAlias("xmlns:ec")
    private final String xmlnsSchema;

    @XStreamAsAttribute
    private final String c;

    @XStreamImplicit
    private final List<Entry> entry;

    public XNFO(String schema, String c, List<Entry> entry) {
        this.xmlnsSchema = schema;
        this.c = c;
        this.entry = entry;
    }

    public void stripComments() {
        List<Comment> oldList = entry.get(0).getCommentsList();
        List<Comment> newList = new ArrayList<>();
        for (Comment comment : oldList) {
            String name = comment.getName();
            if (name.contains("O::")) {
                name = name.replaceAll("[A-Z]::", "");
                comment.setName(name);
                comment.setComment(comment.getComment().strip());
                newList.add(comment);
            }
        }
        entry.get(0).setCommentsList(newList);
    }

    public List<Comment> getCommentList() {
        return this.entry.get(0).getCommentsList();
    }

    /**
     * @return the xmlnsSchema
     */
    public String getXmlnsSchema() {
        return xmlnsSchema;
    }

    /**
     * @return the c
     */
    public String getC() {
        return c;
    }
}
