package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

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
    Map<Long, Long> checkAddressByFias(Long declarationDataId);

    /**
     * Возврашает найденые елементы адреса, используется для определения какой элемент не найден в справочнике
     *
     * @param declarationDataId
     * @return
     */
    Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationDataId);

}
