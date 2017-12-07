package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.List;

/**
 * Сервис для работы со справочником АСНУ
 */
public interface RefBookAsnuService {
    /**
     * Получение доступных (согласно роли пользователя) значений справочника
     *
     * @param userInfo Информация о пользователей
     * @return Список доступных значений справочника
     */
    List<RefBookAsnu> fetchAvailableAsnu(TAUserInfo userInfo);

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Идентификаторы
     * @return Список значений справочника
     */
    List<RefBookAsnu> fetchByIds(List<Long> ids);
}
