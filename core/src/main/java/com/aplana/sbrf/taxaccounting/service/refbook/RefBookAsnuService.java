package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.List;

/**
 * Сервис для работы со справочником АСНУ
 */
public interface RefBookAsnuService {
    /**
     * Получение доступных значений справочника
     *
     * @param userInfo Информация о пользователей
     * @return Список доступных значений справочника
     */
    List<RefBookAsnu> fetchAvailableAsnu(TAUserInfo userInfo);
}
