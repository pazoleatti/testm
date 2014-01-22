package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.DateMaskBoxAbstract;
import com.aplana.gwt.client.mask.parser.MonthYearDateParser;

/**
 * Основной класс для виджета для ввода даты по маске вида dd.MM.yyyy
 *
 * @author aivanov
 */
public class MonthYearMaskBox extends DateMaskBoxAbstract {

    public MonthYearMaskBox() {
        super(MonthYearDateParser.formatMY, MonthYearDateParser.instanceMY(), "99.9999");
    }
}
