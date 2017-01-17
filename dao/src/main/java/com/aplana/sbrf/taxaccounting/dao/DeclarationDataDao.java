package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * Dao-объект для работы с {@link DeclarationData декларациями}
 * @author dsultanbekov
 */
public interface DeclarationDataDao {
	/**
	 * Получить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @return объект декларации
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
	 */
	DeclarationData get(long declarationDataId);

	/**
	 * Сохраняет новую декларацию в БД. 
	 * Этот метод позволяет сохранять только новые декларации (т.е. те, у которых id == null). 
	 * При попытке сохранить уже существующий объект (с непустым id) будет выброшен DaoException
	 * @param declarationData объект декларации
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если передана декларация с непустым id
	 */
	long saveNew(DeclarationData declarationData);
	
    /**
     * Установить статус налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @param state статус налоговой формы
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setStatus(long declarationDataId, State state);
	/**
	 * Удалить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
	 */
	void delete(long declarationDataId);

    Long getRowNumByFilter(DeclarationDataFilter filter, DeclarationDataSearchOrdering ordering, boolean ascSorting, long declarationDataId);

    /**
	 * Данный метод основывая на параметрах фильтра делает поиск в базе и возвращает список идентификаторов данных
	 * по декларациям, соответствующие критериям поиска
	 * @param declarationDataFilter - фильтр, по которому происходит поиск
	 * @param ordering - способ сортировки
	 * @param ascSorting - true, если сортируем по возрастанию, false - по убыванию
	 * @param paginatedSearchParams - диапазон индексов, задающий страницу
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска
	 */
	PagingResult<DeclarationDataSearchResultItem> findPage(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering,
	                                          boolean ascSorting, PagingParams paginatedSearchParams);

    List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting);

    /**
     * Декларация по типу и отчетному периоду подразделения
     */
    List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Декларация по типу и отчетному периоду подразделения + «КПП» и «Налоговый орган» + АСНУ + GUID
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String taxOrganCode, Long asnuId, String guid);

	/**
	 * Получить количество записей, удовлетворяющих запросу
	 * @param filter фильтр, по которому происходит поиск
	 * @return количество записей, удовлетворяющих фильтру
	 */
	int getCount(DeclarationDataFilter filter);

    List<Long> findDeclarationDataByFormTemplate(int templateId, Date startDate);

    /**
     * Получить список id деклараций
     * @param declarationTypeId тип декларации
     * @param departmentId подразделение
     * @return список id деклараций
     */
    List<Long> getDeclarationIds(int declarationTypeId, int departmentId);

    /**
     * Декларация созданная в последнем отчетном периоде подразделения
     */
    DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Поиск декларации необходимых для формирования отчетности для МСФО
     * @param reportPeriodId
     * @return
     */
    List<DeclarationData> getIfrs(int reportPeriodId);

    /**
     * Находим декларации, относящиеся к отчетным периодам, с которыми новый период актуальности версии макета не пересекается
     * @param decTemplateId идентификатор версии макета НФ
     * @param startDate дата, начиная с которой искать пересечения
     * @param endDate дата, до которой искать
     * @return идентификаторы
     */
    List<Integer> findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate);

    /**
     * Обновления комментария НФ
     * @param declarationDataId
     * @param note
     */
    void updateNote(long declarationDataId, String note);

    /**
     * Получение комментария НФ
     * @param declarationDataId
     * @return
     */
    String getNote(long declarationDataId);

}
