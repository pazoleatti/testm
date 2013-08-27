package com.aplana.sbrf.taxaccounting.dao.api;

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
	 * @throws DAOException если периода с заданным идентификатором не существует 
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
	 * Получить список всех налоговых периодов по заданному виду налога за период. Алгоритм: ищет все налоговые периоды,
	 * которые пересекаются с указанным временным интервалом
	 * @param taxType вид налога
	 * @param from дата начала
	 * @param to дата конца
	 * @return  список налогововых периодов по данному виду налога за определенный период
	 */
	List<TaxPeriod> listByTaxTypeAndDate(TaxType taxType, Date from, Date to);

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
