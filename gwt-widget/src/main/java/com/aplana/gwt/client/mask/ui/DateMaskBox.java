package com.aplana.gwt.client.mask.ui;

import com.aplana.gwt.client.mask.DateMaskBoxAbstract;
import com.aplana.gwt.client.mask.parser.DMYDateParser;

/**
 * Основной класс для виджета для ввода даты по маске вида dd.MM.yyyy
 *
 * @author aivanov
 */
public class DateMaskBox extends DateMaskBoxAbstract {

    public DateMaskBox() {
        super(DMYDateParser.formatDMY, DMYDateParser.instanceDMY(), "99.99.9999");
    }
}
