package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;

import java.util.List;

/**
 * DAO-Интерфейс для работы с историей событий деклараций
 */
public interface LogBusinessDao {

	/**
	 * Получить информацию об историей событий деклараций
	 * @param declarationId идентификатор пользователя
	 * @return объект, представляющий историю событий для декларации
	 */
	List<LogBusiness> getDeclarationLogsBusiness(long declarationId);

	/**
	 * Получить информацию об историей событий налоговой формы
	 * @param formId идентификатор пользователя
	 * @return объект, представляющий историю событий для декларации
	 */
	List<LogBusiness> getFormLogsBusiness(long formId);

	/**
	 * добавить информацию в историю событий деклараций
	 * @param logBusiness информация для хранения истории
	 */
	void add(LogBusiness logBusiness);
}
