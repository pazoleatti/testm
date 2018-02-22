package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником АСНУ
 */
@ScriptExposed
public interface RefBookAsnuService {
    /**
     * Возвращяет доступные (согласно роли пользователя) записи справочника
     *
     * @param userInfo информация о пользователей
     * @return список доступных значений справочника
     */
    List<RefBookAsnu> fetchAvailableAsnu(TAUserInfo userInfo);

    /**
     * Возвращяет записи справочника по идентификаторам
     *
     * @param ids идентификаторы
     * @return список записей справочника
     */
    List<RefBookAsnu> fetchByIds(List<Long> ids);

    /**
     * Получение записи справочника по идентификатору
     *
     * @param id идентификатор
     * @return запись справочника или null
     */
    RefBookAsnu fetchById(Long id);

    /**
     * Возвращяет запись справочника по наименованию
     * (удаляет лишние символы при поиске ({@link com.aplana.sbrf.taxaccounting.model.util.StringUtils#cleanString(String)}))
     *
     * @param name наименование АСНУ
     * @return запись справочника или null
     */
    RefBookAsnu fetchByName(String name);
}
