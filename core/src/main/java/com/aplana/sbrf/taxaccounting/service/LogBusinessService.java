package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Сервис для работы с историей событий налоговых форм/деклараций
 */
public interface LogBusinessService {
	/**
	 * Получить информацию об историей событий деклараций
	 * @param declarationId идентификатор декларации
	 * @return объект, представляющий историю событий для декларации
	 */
	List<LogBusiness> getDeclarationLogsBusiness(long declarationId);

	/**
	 * Получить информацию об историей событий налоговой формы
	 * @param formId идентификатор формы
	 * @return объект, представляющий историю событий для налоговой формы
	 */
	List<LogBusiness> getFormLogsBusiness(long formId);

    /**
     * Получает историю изменений для набора НФ и деклараций.
     * Необходимо для ЖА контролеров.
     * @param userInfo
     * @param filter
     * @return
     */
    PagingResult<LogSearchResultItem> getLogsBusiness(TAUserInfo userInfo, LogBusinessFilterValues filter);

	/**
	 * Добавить информацию об логировании
	 * @param formDataId идентификатор формы
	 * @param declarationId идентификатор декларации
	 * @param userInfo информация о пользователе, инициирующего событие
	 * @param event событие
	 * @param note текст
	 */
	void add(Long formDataId, Long declarationId, TAUserInfo userInfo, FormDataEvent event, String note);
}
