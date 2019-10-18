package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.dto.Declaration2NdflFLDTO;
import com.aplana.sbrf.taxaccounting.model.filter.Declaration2NdflFLFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Dao-объект для работы с {@link DeclarationData декларациями}
 */
public interface DeclarationDataDao extends PermissionDao {
    String DECLARATION_NOT_FOUND_MESSAGE = "Налоговая форма с номером = %d не существует либо была удалена";

    /**
     * Получить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @return объект декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
     */
    DeclarationData get(long declarationDataId);

    /**
     * Получить декларации
     *
     * @param declarationDataIds идентификатор декларации
     * @return объект декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
     */
    List<DeclarationData> get(List<Long> declarationDataIds);

    /**
     * Возвращяет список КПП, включаемые в КНФ
     *
     * @param declarationDataId ид КНФ
     * @return список КПП
     */
    List<String> getDeclarationDataKppList(long declarationDataId);

    /**
     * Сохраняет список КПП, включаемые в КНВ
     *
     * @param declarationDataId ид КНФ
     * @param kppSet            набор включаемых в КНФ КПП
     */
    void createDeclarationDataKppList(final long declarationDataId, final Set<String> kppSet);

    /**
     * Возвращяет список ид ФЛ, включаемые в КНФ
     *
     * @param declarationDataId ид КНФ
     * @return список ид ФЛ из реестра
     */
    List<Long> getDeclarationDataPersonIds(long declarationDataId);

    /**
     * Сохраняет новую декларацию в БД.
     * Этот метод позволяет сохранять только новые декларации (т.е. те, у которых id == null).
     * При попытке сохранить уже существующий объект (с непустым id) будет выброшен DaoException
     *
     * @param declarationData объект декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если передана декларация с непустым id
     */
    void create(DeclarationData declarationData);

    /**
     * Установить статус налоговой формы
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param state             статус налоговой формы
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setStatus(long declarationDataId, State state);

    /**
     * Установить ссстояние ЭД налоговой формы
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param docStateId        ссстояние ЭД
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setDocStateId(long declarationDataId, Long docStateId);

    /**
     * Установить имя файла
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param fileName          имя файла
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setFileName(long declarationDataId, String fileName);

    /**
     * Удалить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
     */
    void delete(long declarationDataId);

    /**
     * @deprecated см. {@link #findAllIdsByFilter(DeclarationDataFilter)}
     */
    @Deprecated
    List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting);

    /**
     * Возвращяет страницу списка форм по фильтру
     *
     * @param declarationDataFilter фильтр, по которому происходит поиск
     * @param pagingParams          диапазон индексов, поле сортировки, прямой/обратный порядок
     * @return Страница списка идентификаторов данных по декларациям, соответствующие критериям поиска
     */
    PagingResult<DeclarationDataJournalItem> findPage(DeclarationDataFilter declarationDataFilter, PagingParams pagingParams);

    /**
     * Возвращяет страницу форм 2-НДФЛ (ФЛ)
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации
     * @return страница форм 2-НДФЛ (ФЛ)
     */
    PagingResult<Declaration2NdflFLDTO> findAll2NdflFL(Declaration2NdflFLFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет список ид форм по фильтру
     *
     * @param filter фильтр
     * @return список ид форм
     */
    List<Long> findAllIdsByFilter(DeclarationDataFilter filter);

    /**
     * Возвращяет список форм по типу и отчетному периоду подразделения
     *
     * @param declarationTypeId        типу формы
     * @param departmentReportPeriodId отчетный период подразделения
     * @return список форм
     */
    List<DeclarationData> findAllByTypeIdAndPeriodId(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Возвращяет консолидированную форму в отчетном периоде подразделения и по типу КНФ
     *
     * @param knfType                  тип КНФ
     * @param departmentReportPeriodId отчетный период подразделения
     * @return консолидированная форма
     */
    DeclarationData findKnfByKnfTypeAndPeriodId(RefBookKnfType knfType, int departmentReportPeriodId);

    /**
     * Возвращяет консолидированную форму по параметрам отчетного периода и по типу КНФ
     *
     * @param knfType   тип КНФ
     * @param drpFilter параметры отчетного периода
     * @return консолидированная форма
     */
    DeclarationData findKnfByKnfTypeAndPeriodFilter(RefBookKnfType knfType, DepartmentReportPeriodFilter drpFilter);

    /**
     * Возвращяет список форм по типу и отчетному периоду и паре кпп/октмо
     *
     * @param declarationTypeId типу формы
     * @param reportPeriodId    отчетный период
     * @param kpp               КПП
     * @param oktmo             ОКТМО
     * @return список форм
     */
    List<DeclarationData> findAllByTypeIdAndReportPeriodIdAndKppAndOktmo(int declarationTypeId, int reportPeriodId, String kpp, String oktmo);

    /**
     * Возвращяет список форм по указанным параметрам.
     * Для определения предыдущего ОНФ при формировании аннулирующей 2НДФЛ
     *
     * @param declarationTypeId     ТекущаяОНФ."Макет формы"."Вид формы"
     * @param reportPeriodTypeCode  ТекущаяОНФ."Период"."Код Периода"
     * @param year                  ТекущаяОНФ."Период"."Год"
     * @param kpp                   ТекущаяОНФ.КПП
     * @param oktmo                 ТекущаяОНФ.ОКТМО
     * @return список форм
     */
    List<DeclarationData> findONFFor2Ndfl(int declarationTypeId, String reportPeriodTypeCode, int year, String kpp, String oktmo);

    /**
     * Возвращяет предыдущую форму того же типа и КПП/ОКТМО в состоянии ЭД из заданного множества
     *
     * @return форма
     */
    DeclarationData findPrev(DeclarationData declarationData, RefBookDocState... docStates);

    /**
     * Возвращяет список форм по типу и отчетному периоду подразделения и списку пар кпп/октмо
     *
     * @param declarationTypeId        типу формы
     * @param departmentReportPeriodId отчетный период подразделения
     * @param kppOktmoPairs            список пар кпп/октмо
     * @return список форм
     */
    List<DeclarationData> findAllByTypeIdAndPeriodIdAndKppOktmoPairs(int declarationTypeId, int departmentReportPeriodId, List<Pair<String, String>> kppOktmoPairs);

    /**
     * Декларация по типу и отчетному периоду подразделения + «КПП» и «Налоговый орган» + АСНУ + GUID
     */
    DeclarationData find(int declarationTypeId,
                         int departmentReportPeriodId,
                         String kpp,
                         String oktmo,
                         String taxOrganCode,
                         Long asnuId,
                         String fileName);

    List<Long> findDeclarationDataByFormTemplate(int templateId, Date startDate);

    /**
     * Получить список id деклараций
     *
     * @param declarationTypeId тип декларации
     * @param departmentId      подразделение
     * @return список id деклараций
     */
    List<Long> getDeclarationIds(int declarationTypeId, int departmentId);

    /**
     * Находим декларации, относящиеся к отчетным периодам, с которыми новый период актуальности версии макета не пересекается
     *
     * @param decTemplateId идентификатор версии макета НФ
     * @param startDate     дата, начиная с которой искать пересечения
     * @param endDate       дата, до которой искать
     * @return идентификаторы
     */
    List<Integer> findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate);

    /**
     * Обновления комментария НФ
     */
    void updateNote(long declarationDataId, String note);

    /**
     * Получение комментария НФ
     */
    String getNote(long declarationDataId);

    /**
     * Обновляет дату последнего изменения данных формы
     *
     * @param declarationDataId Идентификатор вида налоговой формы
     */
    void updateLastDataModified(long declarationDataId);

    /**
     * Найти все формы созданные в отчетном периоде
     */
    List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Поиск ОНФ по имени файла и типу файла
     */
    List<DeclarationData> findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId);

    /**
     * Проверка существования формы
     */
    boolean existDeclarationData(long declarationDataId);

    /**
     * Найти НФ по типу, периоду, и значениям Налоговый орган, КПП, ОКТМО
     */
    List<DeclarationData> find(int declarationTemplate, int departmentReportPeriodId, String taxOrganCode, String kpp, String oktmo);

    /**
     * Проверяет существование НФ по критериям в зависимости от ее вида
     */
    boolean existDeclarationData(DeclarationData declarationData);

    /**
     * Возвращаает id НФ по критериям в зависимости от ее вида
     * @param declarationData создаваемая налоговая форма
     */
    List<Long> findExistingDeclarationsForCreationCheck(DeclarationData declarationData);

    /**
     * Возвращаает IDs НФ по критериям декларации, а также по критериям года отчетного периода без учета вида отчетности
     *
     * @param declarationData       создаваемая налоговая форма
     * @param taxPeriodId           ID года периода
     * @param periodCode            код периода
     * @param periodCorrectionDate  дата корректировки периода
     * @return IDs найденных НФ, соответствующих критериям поиска
     */
    List<Long> findExistingDeclarationsForCreationCheck(DeclarationData declarationData, Integer taxPeriodId,
                                                        String periodCode, @Nullable Date periodCorrectionDate);

    /**
     * Находит налоговые формы операции из которых используются для создания Приложения 2 к НП
     *
     * @param reportYear отчетный год
     * @return идентификаторы найденных налоговых форм
     */
    List<Long> findApplication2DeclarationDataId(int reportYear);

    /**
     * Изменяет состояние ЭД форм
     *
     * @param declarationId ид формы
     * @param docStateId    ид состояния ЭД
     */
    void updateDocState(long declarationId, long docStateId);

    /**
     * Возвращяется дату создания формы
     *
     * @param declarationId ид формы
     * @return дата создания формы
     */
    Date getCreatedDateById(long declarationId);

    /**
     * Возвращяет список ид форм 2-НДФЛ (1) для создания формы 2-НДФЛ (ФЛ)
     *
     * @param reportPeriodId ид отчетного периода
     * @param personId       ид ФЛ
     * @param kppOktmoPairs  пары КПП/ОКТМО
     * @return список ид форм
     */
    List<Long> findAllIdsFor2NdflFL(int reportPeriodId, long personId, List<KppOktmoPair> kppOktmoPairs);
}
