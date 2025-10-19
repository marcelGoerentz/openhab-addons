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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link XNFO} class manages .
 *
 * @author Marcel Goerentz - Initial contribution
 */
@XStreamAlias("xnfo")
public class XNFO {

    @XStreamImplicit(itemFieldName = "e")
    private List<Entry> entries;

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public void stripComments() {
        List<Comment> oldList = entries.getFirst().getConfig().getCm();
        List<Comment> newList = new ArrayList<>();
        for (Comment comment : oldList) {
            String name = comment.getN();
            if (name.contains("O::")) {
                name = name.replaceAll("[A-Z]::", "");
                comment.setN(name);
                if (null != comment.getText()) {
                    comment.setText(comment.getText().strip());
                } else {
                    comment.setText("");
                }

                newList.add(comment);
            }
        }
        entries.getFirst().getConfig().setCm(newList);
    }
}
