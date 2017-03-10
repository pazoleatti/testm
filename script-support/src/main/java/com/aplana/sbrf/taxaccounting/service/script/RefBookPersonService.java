package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface RefBookPersonService {


    NaturalPerson identificatePerson(IdentityPerson personData, List<IdentityPerson> refBookPersonList, int tresholdValue, Logger logger);

    NaturalPerson identificatePerson(IdentityPerson personData, List<IdentityPerson> refBookPersonList, int tresholdValue, WeigthCalculator<IdentityPerson> weigthComporators, Logger logger);

    List<BaseWeigthCalculator> getBaseCalculateList();


    /**
     * @param version
     */
    void fillRecordVersions(Date version);

    /**
     * Найти всех новых ФЛ из РНУ-НДФЛ по которым будет создаваться запись в справочнике
     *
     * @param declarationDataId идентификатор НФ
     * @param version           версия записи
     * @return
     */
    List<NaturalPerson> findPersonForInsertFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> naturalPersonPrimaryRnuRowMapper);

    /**
     * Найти всех ФЛ по определяющим параметрам
     *
     * @param declarationDataId идентификатор НФ
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     *
     * @param declarationDataId
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти данные о ФЛ в ПНФ
     *
     * @param declarationDataId
     * @param naturalPersonRowMapper
     * @return
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);


    void fillRecordVersions1151111(Date version);

    List<NaturalPerson> findPersonForInsertFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> naturalPersonPrimaryRnuRowMapper);

    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    List<NaturalPerson> findNaturalPersonPrimaryDataFrom1151111(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);


}
