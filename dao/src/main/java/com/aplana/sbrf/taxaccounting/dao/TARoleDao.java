package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.TARole;

import java.util.List;

/**
 * ДАО для работы с ролями пользователей
 */
public interface TARoleDao {
	/**
	 *
	 * @param id идентификатор роли
	 * @return роль
	 */
	TARole getRole(Integer id);

	/**
	 * возвращает список идентификаторов всех ролей
	 * @return список идентификаторов ролей
	 */
	List<Integer> getAll();
}
