package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataJournalItem;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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
     * @param knfType                  типу КНФ
     * @param departmentReportPeriodId отчетный период подразделения
     * @return консолидированная форм
     */
    DeclarationData findKnfByKnfTypeAndPeriodId(RefBookKnfType knfType, int departmentReportPeriodId);

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
}
