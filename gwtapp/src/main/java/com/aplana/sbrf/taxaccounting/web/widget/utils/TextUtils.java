package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.impl.TextBoxImpl;

/**
 * Нужные методы для манипуляций с текстовыми элементам и самим текстом
 * Корректно работает в IE8, если подцеплен <inherits name="com.google.gwt.user.TextBox"/>
 *
 * @author aivanov
 */
public class TextUtils {

    private static TextBoxImpl impl = GWT.create(TextBoxImpl.class);

    public static int getCursorPos(Element elem) {
        return impl.getCursorPos((com.google.gwt.user.client.Element) elem);
    }

    public static int getSelectionLength(Element elem) {
        return impl.getSelectionLength((com.google.gwt.user.client.Element) elem);
    }

    public static void setCursorPos(Element input, int position) {
        if (position < 0) {
            position = 0;
        }
        impl.setSelectionRange((com.google.gwt.user.client.Element) input, position, 0);
    }

    public static String getSelectedText(InputElement elem) {
        int start = getCursorPos(elem);
        if (start < 0) {
            return "";
        }
        int length = getSelectionLength(elem);
        return elem.getValue().substring(start, start + length);
    }

    public static int getSelectionStart(InputElement elem) {
        if (getSelectionLength(elem) <= 0) {
            return 0;
        }
        return elem.getValue().indexOf(TextUtils.getSelectedText(elem));
    }

}
