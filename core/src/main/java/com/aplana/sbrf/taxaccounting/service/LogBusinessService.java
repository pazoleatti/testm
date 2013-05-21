package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;

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
	List<LogBusiness> getDeclarationLogsBusiness(int declarationId);

	/**
	 * Получить информацию об историей событий налоговой формы
	 * @param formId идентификатор формы
	 * @return объект, представляющий историю событий для налоговой формы
	 */
	List<LogBusiness> getFormLogsBusiness(int formId);

	/**
	 * добавить информацию в историю событий декларации/налоговой формы
	 * если нужно добавить декларацию, тогда в logBusiness должно быть declarationId отличноеот null, и formId равное null
	 * для формы vise versa
	 * @param logBusiness информация для хранения истории
	 */
	void add(LogBusiness logBusiness);
}
