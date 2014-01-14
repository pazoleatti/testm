package com.aplana.gwt.client.mask;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.testing.PassthroughParser;
import com.google.gwt.text.shared.testing.PassthroughRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Виджет для ввода текста с маской
 *
 * @author aivanov
 */
public class MaskTextBox extends MaskBox<String> {


    public MaskTextBox() {
        super(Document.get().createTextInputElement(), PassthroughRenderer.instance(), PassthroughParser.instance());
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
    }

}
