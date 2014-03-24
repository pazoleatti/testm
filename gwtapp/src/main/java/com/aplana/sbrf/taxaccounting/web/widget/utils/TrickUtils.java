package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

/**
 * Класс для всяческих триков и хаков, которые уже исправлены в новых версиях GWT
 * Например, http://code.google.com/p/google-web-toolkit/issues/detail?id=6869
 * Дефолтный класс
 * @author aivanov
 */
public class TrickUtils {

    public static final TrickUtils impl = GWT.create(TrickUtils.class);

    public String getOpacity(Element element) {
        return element.getStyle().getOpacity();
    }

}
