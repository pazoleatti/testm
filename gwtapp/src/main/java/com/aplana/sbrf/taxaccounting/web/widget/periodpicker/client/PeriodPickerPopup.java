package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.HasEnabled;

import java.util.Date;

public interface PeriodPickerPopup extends PeriodPicker, HasEnabled, HasHandlers{

    /**
     * Получить начальную и конечную дату периода доспутной по reportPeriodId
     * @param reportPeriodId идентификатор отчетног опериода
     * @return пара значений дат: начало и конец отченого периода
     */
    Pair<Date, Date> getPeriodDates(Integer reportPeriodId);

    /**
     * Возвращает текстовое представление для выбранных элементов
     * @return
     */
    String getText();
}
