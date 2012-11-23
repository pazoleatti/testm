package com.aplana.sbrf.taxaccounting.dao.mapper;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReportPeriodMapper {
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param periodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws DAOException если периода с заданным идентификатором не существует
	 */
	@Select("select * from report_period where id = #{periodId}")
	ReportPeriod get(@Param("periodId")int periodId);

	/**
	 * Получить объект текущего отчётного периода по виду налога
	 * @param taxType вид налога
	 * @return объект представляющий текущий отчётный период по заданному виду налога, или null, если такого периода нет (еще не открыли)
	 * @throws DAOException если в БД несколько открытых периодов по заданному виду налога
	 */
	@Select("select * from report_period where tax_type = #{taxType} ")
	ReportPeriod getCurrentPeriod(@Param("taxType")TaxType taxType);

	/**
	 * Получить полный список отчётных периодов по виду налога
	 * @param taxType вид налога
	 * @return список всех отчётных периодов
	 */
	@Select("select * from report_period where tax_type = #{taxType} ")
	List<ReportPeriod> listAllPeriodsByTaxType(TaxType taxType);
}
