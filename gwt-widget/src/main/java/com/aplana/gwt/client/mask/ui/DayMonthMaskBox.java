package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.parser.DMDateParser;

/**
 * Основной класс для виджета для ввода даты по маске вида dd.MM
 *
 * @author vpetrov
 */
public class DayMonthMaskBox extends DateMaskBox {

    public DayMonthMaskBox() {
        super(DMDateParser.formatDM, DMDateParser.instanceDM(), "99.99");
    }
}
