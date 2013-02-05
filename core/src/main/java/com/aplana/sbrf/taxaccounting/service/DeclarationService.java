package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Сервис для работы с {@link налоговыми декларациями Declaration}
 * @author dsultanbekov
 */
public interface DeclarationService {
	/**
	 * Создать декларацию в текущем отчётном периоде. Созданная декларация сразу же сохраняется в БД и возвращается идентификатор созданной записи.
	 * @param logger - объект журнала
	 * @param taxType вид налога
	 * @param departmentId идентификатор подразделения, в котором создаваётся декларация
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @return идентификатор созданной декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на создание декларации с заданными параметрами
	 * 	ServiceException - если при создании декларации произошла ошибка (например декларация с такими параметрами уже существует)
	 */
	long createDeclaration(Logger logger, TaxType taxType, int departmentId, int userId);
	/**
	 * Получить декларацию
	 * @param declarationId
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	Declaration get(long declarationId, int userId);
	/**
	 * Установить в декларации флаг принятия
	 * @param declarationId идентификатор декларации
	 * @param accepted значение флага
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
	 */
	void setAccepted(long declarationId, boolean accepted, int userId);
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return строка, содержащая данные декларации в формате законодателя
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlData(long declarationId, int userId);
	/**
	 * Получить печатное представление данные декларации.
	 * Получается путём подстановки данных декларации в формате xml в Jasper-reports отчёт, шаблона декларации
	 * @param declarationId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return файл Xlsx в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getXlsxData(long declarationId, int userId);
}
