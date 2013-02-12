package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Dao-объект для работы с {@link Declaration декларациями}
 * @author dsultanbekov
 */
public interface DeclarationDao {
	/**
	 * Получить декларацию
	 * @param declarationId идентификатор декларации
	 * @return объект декларации
	 * @throws DaoException если такой декларации не существует
	 */
	Declaration get(long declarationId);
	
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationId идентификатор декларации
	 * @return данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	String getXmlData(long declarationId);
	
	/**
	 * Сохраняет новую декларацию в БД. 
	 * Этот метод позволяет сохранять только новые декларации (т.е. те, у которых id == null). 
	 * При попытке сохранить уже существующий объект (с непустым id) будет выброшен DaoException
	 * @param declaration объект декларации
	 * @return идентификатор сохранённой записи
	 * @throws DaoException если передана декларация с непустым id
	 */
	long saveNew(Declaration declaration);
	
	/**
	 * Установить флаг принятия декларации
	 * @param declarationId идентификатор декларации
	 * @param accepted признак принятия
	 * @throws DaoException если такой декларации не существует
	 */
	void setAccepted(long declarationId, boolean accepted);
	
	/**
	 * Задать данные декларации в формате законодателя (XML)
	 * @param declarationId идентификтор декларации
	 * @param xmlData данные декларации в формате законодателя
	 * @throws DaoException если такой декларации не существует
	 */
	void setXmlData(long declarationId, String xmlData);
	
	/**
	 * Удалить декларацию
	 * @param declarationId идентификатор декларации
	 * @throws DaoException если такой декларации не существует
	 */
	void delete(long declarationId);

	/**
	 * Данный метод основывая на параметрах фильтра делает поиск в базе и возвращает список идентификаторов данных
	 * по декларациям, соответствующие критериям поиска
	 * @param declarationFilter - фильтр, по которому происходит поиск
	 * @param ordering - способ сортировки
	 * @param ascSorting - true, если сортируем по возрастанию, false - по убыванию
	 * @param paginatedSearchParams - диапазон индексов, задающий страницу
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска
	 */
	PaginatedSearchResult<DeclarationSearchResultItem> findPage(DeclarationFilter declarationFilter, DeclarationSearchOrdering ordering,
	                                          boolean ascSorting, PaginatedSearchParams paginatedSearchParams);

	/**
	 * Получить количество записей, удовлетворяющих запросу
	 * @param filter фильтр, по которому происходит поиск
	 * @return количество записей, удовлетворяющих фильтру
	 */
	long getCount(DeclarationFilter filter);
}
