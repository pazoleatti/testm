package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;

/**
 * Перенос данных из предыдущей НФ для корр. периодов
 */
public class FillPreviousAction extends AbstractDataRowAction implements ActionName {
    @Override
    public String getName() {
        return "Заполнение данных из найденной формы предыдущего периода";
    }
}
