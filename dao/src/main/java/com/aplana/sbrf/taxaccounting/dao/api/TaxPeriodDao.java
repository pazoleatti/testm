package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * DAO-интерфейс для работы с налоговыми периодами
 * @author dsultanbekov
 */
public interface TaxPeriodDao {
	/**
	 * Получить объект налогового периода по идентификатору периода
	 * @param taxPeriodId идентификатор налогового периода
	 * @return объект, задаваемый идентификатором
	 * @throws DaoException если периода с заданным идентификатором не существует
	 */
	TaxPeriod get(int taxPeriodId);
	
	/**
	 * Получить список всех налоговых периодов по заданному виду налога.
	 * Список будет отсортирован по убыванию даты начала периода
	 * @param taxType вид налога
	 * @return список налоговых периодов по данному виду налога, отсортированный по убыванию даты начала периодоа
	 */
	List<TaxPeriod> listByTaxType(TaxType taxType);

	/**
	 * Получить налоговые периоды
	 * @param taxType тип налога
	 * @param year год
	 * @return список налоговых периодов по типу налога за год
	 */
	public List<TaxPeriod> listByTaxTypeAndYear(TaxType taxType, int year);

	/**
	 * Добавить новый налоговый период
	 * @param taxPeriod налоговый период
	 * @return идентификатор нового налогового периода
	 */
	int add(TaxPeriod taxPeriod);

	/**
	 *  Получить последний налоговый период по виду налога
	 *  @param taxType вид налога
	 */
	TaxPeriod getLast(TaxType taxType);
}
