package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.impl.TextBoxImpl;

import java.util.Collection;
import java.util.Iterator;

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

    /**
     * Сединение коллекции строк в одну разделенными ;
     * @param strings массив строк
     * @return соединенная строка
     */
    public static String joinListToString(Collection<String> strings) {
        if ((strings == null) || strings.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();

        if (strings.size() == 1) {
            text.append(strings.iterator().next());
        } else {
            int i = 0;
            Iterator<String> iterator = strings.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                text.append(name);
                if (i < strings.size() - 1) {
                    text.append("; ");
                }
                i++;
            }
        }
        return text.toString();
    }

    /**
     * Генерация для высплывающей подписи для текстБокса
     * @param text строка из joinListToString
     * @return красивая подпись
     */
    public static String generateTextBoxTitle(String text) {
        String title = text;
        if (text != null && text.contains("; ")) {
            text = text.replace("; ", ";\n");
            title = "Выбрано: " + text.split(";").length + "\n" + text;
        }
        return title;
    }

    /**
     * Генерация для высплывающей подписи для текстБокса для HTML представления
     * @param text строка из joinListToString
     * @return красивая подпись
     */
    public static String generateTextBoxHTMLTitle(String text) {
        String title = text;
        if (text != null && text.contains("; ")) {
            text = text.replace("; ", ";<br/>");
            title = "Выбрано: " + text.split(";").length + "<br/>" + text;
        }
        return title;
    }

}
