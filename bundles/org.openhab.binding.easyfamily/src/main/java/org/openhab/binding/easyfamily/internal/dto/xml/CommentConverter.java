package org.openhab.binding.easyfamily.internal.dto.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CommentConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Comment.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Comment comment = (Comment) source;
        writer.addAttribute("n", comment.getN());
        writer.setValue(comment.getText());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Comment comment = new Comment();
        comment.setN(reader.getAttribute("n"));
        comment.setText(reader.getValue());
        return comment;
    }
}
