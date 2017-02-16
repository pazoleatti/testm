package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с ОбязПлатСВ
 */
@ScriptExposed
public interface RaschsvObyazPlatSvService {

    /**
     * Сохранение ОбязПлатСВ
     * @param raschsvObyazPlatSv - Сводные данные об обязательствах плательщика страховых взносов
     * @return
     */
    Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv);

    /**
     * Выборка из ОбязПлатСВ
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    RaschsvObyazPlatSv findObyazPlatSv(Long declarationDataId);

    /**
     * Удаляет данные из связанных таблиц: raschsv_kol_lic_tip и raschsv_sv_sum_1tip
     * @param declarationDataId - идентификатор декларации
     */
    void deleteFromLinkedTable(Long declarationDataId);
}
