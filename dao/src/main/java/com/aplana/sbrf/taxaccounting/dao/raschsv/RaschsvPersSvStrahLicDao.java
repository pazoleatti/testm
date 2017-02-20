package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;

import java.util.List;
import java.util.Map;

/**
 * DAO-интерфейс для работы с ПерсСвСтрахЛиц
 */
public interface RaschsvPersSvStrahLicDao {

    /**
     * Получить ПерсСвСтрахЛиц по id
     * @param id
     * @return возвращает объект RaschsvPersSvStrahLic с проиницилизированными сведениями о выплатах
     */
    RaschsvPersSvStrahLic get(long id);

    /**
     * Сохранение ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicList
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);

    /**
     * Обновление ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicListList
     * @return
     */
    Integer updatePersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicListList);

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
     * Найти физлиц по параметрам спецотчета
     * @param declarationDataId - идентификатор декларации
     * @param params - параметры спецотчета
     * @return
     */
    List<RaschsvPersSvStrahLic> findPersonBySubreportParams(Long declarationDataId, Map<String, Object> params);
}
