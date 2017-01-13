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

    /**
     * Выборка "Персонифицированных сведений о застрахованных лицах"
     * @param declarationDataId - идентификатор декларации
     * @param innfl - ИНН ФЛ
     * @return
     */
    RaschsvPersSvStrahLic findPersonByInn(Long declarationDataId, String innfl);

    /**
     * Выборка "Персонифицированных сведений о застрахованных лицах"
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId);
}
