package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;

/**
 * Сервис для работы с "Персонифицированные сведения о застрахованных лицах"
 */
public interface RaschsvPersSvStrahLicService {

    /**
     * Сохраняет перечень записей "Персонифицированные сведения о застрахованных лицах"
     * @param raschsvPersSvStrahLicList - перечень записей "Персонифицированные сведения о застрахованных лицах"
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);
}
