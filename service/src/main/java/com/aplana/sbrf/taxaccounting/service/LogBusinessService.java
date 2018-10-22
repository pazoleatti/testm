package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с историей событий налоговых форм/деклараций
 */
@ScriptExposed
public interface LogBusinessService {
    /**
     * Получить информацию об истории событий деклараций
     *
     * @param declarationId идентификатор декларации
     * @param ordering      столбец, по которому сортировать
     * @param isAscSorting  сорировать по возрастанию или убыванию
     * @return объект, представляющий историю событий для декларации
     */
    List<LogBusiness> getDeclarationLogsBusiness(long declarationId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting);

    /**
     * Получить информацию об истории событий налоговой формы
     *
     * @param formId       идентификатор формы
     * @param ordering     столбец, по которому сортировать
     * @param isAscSorting сорировать по возрастанию или убыванию
     * @return объект, представляющий историю событий для налоговой формы
     */
    List<LogBusiness> getFormLogsBusiness(long formId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting);

	/**
	 * Добавить информацию об логировании
	 * @param formDataId идентификатор формы
	 * @param declarationId идентификатор декларации
	 * @param userInfo информация о пользователе, инициирующего событие
	 * @param event событие
	 * @param note текст
	 */
	void add(Long formDataId, Long declarationId, TAUserInfo userInfo, FormDataEvent event, String note);

    /**
     * Получить дату создания файла
     * @param declarationDataId
     * @return
     */
    Date getFormCreationDate(long declarationDataId);
	/**
	 * Получить имя пользователя, загрузившего ТФ
     * @param declarationDataId код декларации
	 */
	String getFormCreationUserName(long declarationDataId);

	/**
	 * Получить информацию об истории событий налоговой формы
	 *
	 * @param declarationId       идентификатор формы
	 * @param pagingParams     данные для сортировки

	 * @return  список  истории событий для налоговой формы
	 */
	List<LogBusiness> getDeclarationLogsBusiness(long declarationId, PagingParams pagingParams);
}
