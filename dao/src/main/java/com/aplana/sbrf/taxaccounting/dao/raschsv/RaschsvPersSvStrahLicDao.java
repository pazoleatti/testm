package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Персонифицированные сведения о застрахованных лицах"
 */
public interface RaschsvPersSvStrahLicDao {

    /**
     * Выгрузка всех сведений о застрахованных лицах
     * @return
     */
    List<RaschsvPersSvStrahLic> findAll();

    /**
     * Сохранение "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicList - перечень сведений о застрахованных лицах
     * @return
     */
    Integer insert(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);
}
