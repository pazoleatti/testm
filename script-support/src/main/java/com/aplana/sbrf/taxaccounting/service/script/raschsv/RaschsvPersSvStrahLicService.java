package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с ПерсСвСтрахЛиц
 */
@ScriptExposed
public interface RaschsvPersSvStrahLicService {

    /**
     * Получить ПерсСвСтрахЛиц по id
     * @param id
     * @return возвращает объект RaschsvPersSvStrahLic с проиницилизированными сведениями о выплатах
     */
    RaschsvPersSvStrahLic get(long id);

    /**
     * Сохраняет перечень записей ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicList
     * @return
     */
    Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList);

    /**
     * Обновление ПерсСвСтрахЛиц
     * @param raschsvPersSvStrahLicListList
     * @return
     */
    Integer updateRefBookPersonReferences(List<NaturalPerson> raschsvPersSvStrahLicListList);

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
    List<RaschsvPersSvStrahLic> findPersonBySubreportParams(Long declarationDataId, Map<String, Object> params);

    /**
     * Найти дубли в одной форме
     * @param declarationDataId
     * @return
     */
    List<RaschsvPersSvStrahLic> findDublicatePersonsByDeclarationDataId(long declarationDataId);

    /**
     * Найти дубли в одной в разных формах
     * @param declarationDataId
     * @param reportPeriodId
     * @return
     */
    List<RaschsvPersSvStrahLic> findDublicatePersonsByReportPeriodId(long declarationDataId, long reportPeriodId);
}
