package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;

import java.util.List;

/**
 * DAO-интерфейс для работы c ОбязПлатСВ
 */
public interface RaschsvObyazPlatSvDao {

    /**
     * Сохранение ОбязПлатСВ
     * @param raschsvObyazPlatSv - ОбязПлатСВ
     * @return
     */
    Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv);

    /**
     * Выборка из ОбязПлатСВ
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    RaschsvObyazPlatSv findObyazPlatSv(Long declarationDataId);
}
