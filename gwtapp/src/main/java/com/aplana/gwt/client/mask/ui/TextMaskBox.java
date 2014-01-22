package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.MaskBox;
import com.google.gwt.text.shared.testing.PassthroughParser;
import com.google.gwt.text.shared.testing.PassthroughRenderer;

/**
 * Виджет для ввода текста с маской
 *
 * @author aivanov
 */
public class TextMaskBox extends MaskBox<String> {


    public TextMaskBox() {
        super(PassthroughRenderer.instance(), PassthroughParser.instance());
    }

    @Override
    public String getText() {
        return isEqualsTextPicture(super.getText()) ? "" : super.getText();
    }

}
