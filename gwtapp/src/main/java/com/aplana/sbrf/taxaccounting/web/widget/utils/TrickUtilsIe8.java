package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * Класс для всяческих триков и хаков, которые уже исправлены в новых версиях GWT
 * Например, http://code.google.com/p/google-web-toolkit/issues/detail?id=6869
 *
 * Переопредление для ие8
 *
 * @author aivanov
 */
public class TrickUtilsIe8 extends TrickUtils {

    public String getOpacity(Element element) {
        return cssGetOpacity(element.getStyle());
    }

    public native String cssGetOpacity(Style style) /*-{
        return '' + (style.filter.substring(14, style.filter.length - 1) / 100);
    }-*/;

}
