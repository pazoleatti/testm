package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;

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

	/**
	 * Получить список всех видов деклараций
	 * @return список видов деклараций
	 * @throws DaoException если в БД нет такой записи
	 */
	List<DeclarationType> listAll();

	/**
	 * Получить список всех видов деклараций по типу налога
	 * @return список видов деклараций
	 */
	List<DeclarationType> listAllByTaxType(TaxType taxType);
}
