package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;

/**
 * Dao для работы с видами деклараций
 * @author dsultanbekov
 *
 */
public interface DeclarationTypeDao {
	/**
	 * Получить описание вида декларации по идентификатору
	 * @param declarationTypeId идентификатор вида декларации
	 * @return описание вида декларации, с заданным идентификатором
	 * @throws DaoException если в БД нет такой записи
	 */
	DeclarationType get(int declarationTypeId);
}
