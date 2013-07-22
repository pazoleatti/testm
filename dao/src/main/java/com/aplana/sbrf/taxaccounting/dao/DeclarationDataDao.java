package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Dao-объект для работы с {@link DeclarationData декларациями}
 * @author dsultanbekov
 */
public interface DeclarationDataDao {
	/**
	 * Получить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @return объект декларации
	 * @throws DaoException если такой декларации не существует
	 */
	DeclarationData get(long declarationDataId);

	/**
	 * Проверяет, что декларация содержит данные (XML-файл не пуст)
	 * @param declarationDataId идентификатор декларации
	 * @return true если декларация содержит данные, false - в противном случае
	 * В случае, если декларации с указанным id не существует, вернёт false
	 */
	boolean hasXmlData(long declarationDataId);
	
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationDataId идентификатор декларации
	 * @return данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	String getXmlData(long declarationDataId);
	
	/**
	 * Получить отчет в формате XLSX
	 * @param declarationDataId идентификатор декларации
	 * @return данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	byte[] getXlsxData(long declarationDataId);
	
	/**
	 * Получить отчет в формате PDF
	 * @param declarationDataId идентификатор декларации
	 * @return данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	byte[] getPdfData(long declarationDataId);
	
	/**
	 * Сохраняет новую декларацию в БД. 
	 * Этот метод позволяет сохранять только новые декларации (т.е. те, у которых id == null). 
	 * При попытке сохранить уже существующий объект (с непустым id) будет выброшен DaoException
	 * @param declarationData объект декларации
	 * @return идентификатор сохранённой записи
	 * @throws DaoException если передана декларация с непустым id
	 */
	long saveNew(DeclarationData declarationData);
	
	/**
	 * Установить флаг принятия декларации
	 * @param declarationDataId идентификатор декларации
	 * @param accepted признак принятия
	 * @throws DaoException если такой декларации не существует
	 */
	void setAccepted(long declarationDataId, boolean accepted);
	
	/**
	 * Задать данные декларации в формате законодателя (XML)
	 * @param declarationDataId идентификтор декларации
	 * @param xmlData данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	void setXmlData(long declarationDataId, String xmlData);
	
	
	/**
	 * Задать результат формирования отчета в формате XLSX 
	 * @param declarationDataId идентификтор декларации
	 * @param xmlData данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	void setXlsxData(long declarationDataId, byte[] xlsxData);
	
	/**
	 * Задать результат формирования отчета в формате PDF
	 * @param declarationDataId идентификтор декларации
	 * @param xmlData данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	void setPdfData(long declarationDataId, byte[] pdfData);
	
	/**
	 * Удалить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @throws DaoException если такой декларации не существует
	 */
	void delete(long declarationDataId);

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

	/**
	 * Ищет декларацию по заданным параметрам.
	 * @param declarationTypeId идентификатор типа декларации
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 * @param reportPeriodId идентификатор {@link com.aplana.sbrf.taxaccounting.model.ReportPeriod отчетного периода}
	 * @return декларацию или null, если такой декларации не найдено
	 * @throws DaoException если будет найдено несколько записей, удовлетворяющих условию поиска
	 */
	DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId);

	/**
	 * Получить количество записей, удовлетворяющих запросу
	 * @param filter фильтр, по которому происходит поиск
	 * @return количество записей, удовлетворяющих фильтру
	 */
	int getCount(DeclarationDataFilter filter);
}
