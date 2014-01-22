package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.DateMaskBoxAbstract;
import com.aplana.gwt.client.mask.parser.YearDateParser;

/**
 * Основной класс для виджета для ввода даты по маске вида dd.MM.yyyy
 *
 * @author aivanov
 */
public class YearMaskBox extends DateMaskBoxAbstract {

    public YearMaskBox() {
        super(YearDateParser.formatY, YearDateParser.instanceY(), "9999");
    }
}
