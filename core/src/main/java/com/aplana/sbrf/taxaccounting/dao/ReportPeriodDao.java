package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс DAO для работы с {@link ReportPeriod отчётными периодами} 
 * @author dsultanbekov
 */
public interface ReportPeriodDao {
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws DAOException если периода с заданным идентификатором не существует 
	 */
	ReportPeriod get(int reportPeriodId);
	/**
	 * Получить объект текущего отчётного периода по виду налога
	 * @param taxType вид налога
	 * @return объект представляющий текущий отчётный период по заданному виду налога, или null, если такого периода нет (еще не открыли)
	 * @throws DAOException если в БД несколько открытых периодов по заданному виду налога
	 */
	ReportPeriod getCurrentPeriod(TaxType taxType);
	
	/**
	 * Получить полный список отчётных периодов по виду налога 
	 * @param taxType вид налога
	 * @return список всех отчётных периодов
	 */
	List<ReportPeriod> listAllPeriodsByTaxType(TaxType taxType);
}
