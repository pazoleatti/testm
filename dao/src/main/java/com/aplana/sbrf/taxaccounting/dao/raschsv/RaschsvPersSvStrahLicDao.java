package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Персонифицированные сведения о застрахованных лицах"
 */
public interface RaschsvPersSvStrahLicDao {

    /**
     * Сохранение "Персонифицированные сведения о застрахованных лицах"
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
    List<RaschsvPersSvStrahLic> findPersonsByInn(Long declarationDataId, String innfl);

    /**
     * Выборка "Персонифицированных сведений о застрахованных лицах"
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId);
}
