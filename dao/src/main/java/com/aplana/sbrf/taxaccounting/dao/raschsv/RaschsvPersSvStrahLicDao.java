package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;
import java.util.Map;

/**
 * DAO-интерфейс для работы с ПерсСвСтрахЛиц
 */
public interface RaschsvPersSvStrahLicDao {

    /**
     * Сохранение ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicList
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);

    /**
     * Выборка ПерсСвСтрахЛиц
     * @param declarationDataId - идентификатор декларации
     * @param innfl - ИНН ФЛ
     * @return
     */
    RaschsvPersSvStrahLic findPersonByInn(Long declarationDataId, String innfl);

    /**
     * Выборка ПерсСвСтрахЛиц
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId);

    /**
     * Выборка "Персонифицированных сведений о застрахованных лицах"
     * @param declarationDataId - идентификатор декларации
     * @param params - параметры спецотчета
     * @return
     */
    RaschsvPersSvStrahLic findPersonBySubreportParams(Long declarationDataId, Map<String, Object> params);
}
