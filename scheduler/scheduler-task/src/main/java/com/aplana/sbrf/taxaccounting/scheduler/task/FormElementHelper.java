package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBox;

/**
 * Created by auldanov on 25.07.2014.
 */
public interface FormElementHelper {
    /**
     * Создание SelectBox'а со значениями из выборки "20 - Получение ТБ универсальное"
     *
     * @param userInfo
     * @return
     */
    SelectBox getTBSelectBox(TAUserInfo userInfo);
}
