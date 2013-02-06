package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс для проверки прав пользователя на работу с {@link Declaration декларациями}
 * @author dsultanbekov
 */
public interface DeclarationAccessService {
	/**
	 * Проверяет, имеет ли пользователь права на просмотр декларации.
	 * @param userId идентфикатор пользователя
	 * @param declarationId идентификатор декларации
	 * @return true если пользователь имеет права на просмотр декларации, false - в противном случае
	 */
	boolean canRead(int userId, long declarationId);
	
	/**
	 * Проверяет права пользователя для операции создания декларации.
	 * Данная проверка проверяет только полномочия пользователя, бизнес-проверок 
	 * (например, что декларация уже сформирована, или что не хватает каких-то данных не выполняется)
	 * @param taxType вид налога
	 * @param departmentId идентификатор подразделения, в котором формируется декларация
	 * @param reportPeriodId идентфикатор отчётного периода, в котором формируется декларация
	 * @return true, если у пользователя есть права на создание декларации с указанными параметрами, false - в противном случае
	 */
	boolean canCreate(TaxType taxType, int departmentId, int reportPeriodId);
	
	/**
	 * Проверяет, может ли пользователь принять декларацию
	 * @param userId идентификатор пользователя
	 * @param declarationId идентификатор декларации
	 * @return true - если у пользователя есть права на принятие декларации, false - в противном случае
	 */
	boolean canAccept(int userId, long declarationId);
	
	/**
	 * Проверяет, может ли пользователь отменить принятие декларации.
	 * @param userId идентификатор пользователя
	 * @param declarationId идентификатор декларации
	 * @return true - если у пользователя есть права на отмену принятия декларации, false - в противном случае
	 */
	boolean canReject(int userId, long declarationId);
}
