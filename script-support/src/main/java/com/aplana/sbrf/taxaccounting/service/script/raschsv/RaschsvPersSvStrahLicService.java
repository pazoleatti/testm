package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;

/**
 * Сервис для работы с "Персонифицированные сведения о застрахованных лицах"
 */
public interface RaschsvPersSvStrahLicService {

    /**
     * Сохраняет перечень записей "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicList
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);
}
