package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;


import com.aplana.sbrf.taxaccounting.model.Months;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface CreateFormDataUiHandlers extends UiHandlers {
	void onConfirm();
    void onReportPeriodChange();

    /**
     * Проверяет является ли форма ежемесячной.
     * @param formId идентификатор налоговой формы
     * @return true - ежемесячная, false - не ежемесячная
     */
    void isMonthly(Integer formId, Integer reportPeriodId);
}
