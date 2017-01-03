package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Сводные данные об обязательствах плательщика страховых взносов"
 */
public interface RaschsvObyazPlatSvDao {

    /**
     * Выгрузка всех записей из "Персонифицированные сведения о застрахованных лицах"
     * @return
     */
    List<RaschsvObyazPlatSv> findAll();

    /**
     * Сохранение "Сводные данные об обязательствах плательщика страховых взносов"
     * @param raschsvObyazPlatSv - Сводные данные об обязательствах плательщика страховых взносов
     * @return
     */
    Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv);
}
