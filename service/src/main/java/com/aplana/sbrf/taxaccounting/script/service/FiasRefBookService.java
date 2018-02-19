package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Map;

/**
 * @author Andrey Drunk
 */
@ScriptExposed
public interface FiasRefBookService {

    /**
     * Возвращает все найденные в справочнике адреса по записям НФ
     *
     * @param declarationDataId
     * @return
     */
    Map<Long, FiasCheckInfo> checkAddressByFias(Long declarationDataId);

    /**
     * Возврашает найденые елементы адреса, используется для определения какой элемент не найден в справочнике
     *
     * @param declarationDataId
     * @return
     */
    Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationDataId);

    /**
     * Вызывает процедуру обновляющую материальные представления после импорта в ФИАС
     */
    void refreshViews();
}