package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.identification.IdentificationData;
import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeightCalculator;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * Сервис работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface RefBookPersonService {

    /**
     * Очищает данные по ФЛ
     * @param declarationDataId
     */
    void clearRnuNdflPerson(Long declarationDataId);

    NaturalPerson identificatePerson(IdentificationData identificationData, Logger logger);

    NaturalPerson identificatePerson(IdentificationData identificationData, WeightCalculator<IdentityPerson> weightCalculator, Logger logger);

    List<BaseWeightCalculator> getBaseCalculateList();


    void fillRecordVersions();

    /**
     * Найти всех ФЛ по определяющим параметрам
     *
     * @param declarationDataId идентификатор НФ
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     *
     * @param declarationDataId
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти данные о ФЛ в ПНФ
     *
     * @param declarationDataId
     * @param naturalPersonRowMapper
     * @return
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);

    /**
     * Рассчитывает вес, который показывает насколько похоже сравниваемое физлицо с физлицом из списка. Метод ничего не
     * возвращает поскольку вес записывается в поле weight объекта NaturalPerson.
     * @param searchPersonData  физлицо для которого определяется схожесть по весам.
     * @param personDataList    физлица которые были отобраны для сравнения по весам с основным физлицом
     * @param weightCalculator  объект содержащий логику сравнения по весам
     */
    void calculateWeight(NaturalPerson searchPersonData, List<NaturalPerson> personDataList, WeightCalculator<IdentityPerson> weightCalculator);
}
