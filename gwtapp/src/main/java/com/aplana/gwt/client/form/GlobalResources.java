package com.aplana.gwt.client.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Зацепка для стилей
 * @author aivanov on 13.12.13.
 */
public interface GlobalResources extends ClientBundle {

    public static final GlobalResources INSTANCE = GWT.create(GlobalResources.class);

    public interface Style extends CssResource {

        String typicalFormHeader();

        String typicalFormBody();

        String left();

        String right();

        @ClassName("resize-overflow")
        String resizeOverflow();

        @ClassName("child-inherit")
        String childInherit();
    }

    @Source("Form.css")
    Style style();

}
