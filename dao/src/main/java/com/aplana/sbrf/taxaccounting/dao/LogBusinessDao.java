package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * DAO-Интерфейс для работы с историей событий деклараций
 */
public interface LogBusinessDao {

    /**
     * Получить информацию об истории событий деклараций
     *
     * @param declarationId идентификатор пользователя
     * @param ordering      столбец, по которому сортировать
     * @param isAscSorting  сорировать по возрастанию или убыванию
     * @return объект, представляющий историю событий для декларации
     */
    List<LogBusiness> getDeclarationLogsBusiness(long declarationId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting);

    /**
     * Получить информацию об истории событий налоговой формы
     *
     * @param formId       идентификатор пользователя
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return объект, представляющий историю событий для декларации
     */
    List<LogBusiness> getFormLogsBusiness(long formId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting);

	/**
	 * Получить дату последнего принятия налоговой формы
	 * @param formId идентификатор пользователя
	 * @return объект, представляющий дату принятия нф
	 */
	Date getFormAcceptanceDate(long formId);

	/**
	 * Получить дату создания налоговой формы
	 * @param formId идентификатор пользователя
	 * @return объект, представляющий дату принятия нф
	 */
	Date getFormCreationDate(long formId);

	/**
	 * Получить имя пользователя, загрузившего ТФ
	 * @param declarationDataId код декларации
	 */
	String getFormCreationUserName(long declarationDataId);

	/**
	 * добавить информацию в историю событий деклараций
	 * @param logBusiness информация для хранения истории
	 */
	void add(LogBusiness logBusiness);
}
