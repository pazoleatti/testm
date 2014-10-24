package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.Map;

/**
 * Сервис для проверки прав пользователя на изменения записей региональных справочников.
 *
 * @author Ramil Timerbaev
 */
public interface RegionSecurityService {

    /**
     * Проверить права пользователя на изменение записи регионального справочника.
     *
     * @param user пользователь
     * @param refBookId идентификатор справочника
     * @param uniqueRecordId идентификатор версии записи (при удалении/изменении существущей версии записи)
     * @param recordCommonId общий идентификатор всех версии записей (при добавлении новой версии записи)
     * @param values значения версии записи (при добавлении/измении версии записи)
     * @param start дата начала актуальности
     * @param end дата окончания актуальности
     */
    boolean check(TAUser user, Long refBookId, Long uniqueRecordId, Long recordCommonId,
                  Map<String, RefBookValue> values, Date start, Date end);

    /**
     * Проверить права пользователя при удалении записи регионального справочника.
     *
     * @param user пользователь
     * @param refBookId идентификатор споравочника
     * @param uniqueRecordId идентификатор версии записи
     */
    boolean check(TAUser user, Long refBookId, Long uniqueRecordId);
}
