package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс DAO для работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
public interface RefBookPersonDao {

    //--------------------------- РНУ ---------------------------

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
    List<NaturalPerson> findPersonForInsertFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> primaryRowMapper);

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

    //--------------------------- 1151111 ---------------------------

    /**
     * @param declarationDataId
     * @param naturalPersonRowMapper
     * @return
     */
    List<NaturalPerson> findNaturalPersonPrimaryDataFrom1151111(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper);


    /**
     * @param version
     */
    void fillRecordVersions1151111(Date version);

    /**
     * Найти всех новых ФЛ из РНУ-НДФЛ по которым будет создаваться запись в справочнике
     *
     * @param declarationDataId идентификатор НФ
     * @param version           версия записи
     * @return
     */
    List<NaturalPerson> findPersonForInsertFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> primaryRowMapper);

    /**
     * Найти всех ФЛ по определяющим параметрам
     *
     * @param declarationDataId идентификатор НФ
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);

    /**
     * Найти всех ФЛ по полному списку параметров
     *
     * @param declarationDataId
     * @param asnuId            идентификатор АСНУ загрузившей данные
     * @param version           версия записи
     * @return
     */
    Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler);


}
