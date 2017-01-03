package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Персонифицированные сведения о застрахованных лицах"
 */
public interface RaschsvPersSvStrahLicDao {

    /**
     * Сохранение "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicList - перечень "Персонифицированные сведения о застрахованных лицах"
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);
}
