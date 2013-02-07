package com.aplana.sbrf.taxaccounting.dao.mapper;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
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
	@ResultMap("reportPeriodMap")
	ReportPeriod get(@Param("periodId")int periodId);

	/**
	 * Получить объект текущего отчётного периода по виду налога
	 * @param taxTypeCode код вида налога
	 * @return объект представляющий текущий отчётный период по заданному виду налога, или null, если такого периода нет (еще не открыли)
	 * @throws DAOException если в БД несколько открытых периодов по заданному виду налога
	 */
	@Select("select * from report_period where tax_type = #{taxTypeCode} and is_active = 1 ")
	@ResultMap("reportPeriodMap")
	ReportPeriod getCurrentPeriod(@Param("taxTypeCode")char taxType);

	/**
	 * Получить полный список отчётных периодов по виду налога
	 * @param taxTypeCode код вида налога
	 * @return список всех отчётных периодов
	 */
	@Select("select * from report_period where tax_type = #{taxTypeCode} ")
	@ResultMap("reportPeriodMap")
	List<ReportPeriod> listAllPeriodsByTaxType(@Param("taxTypeCode")char taxTypeCode);

	/**
	 * Возвращает список отчётных периодов, входящий в данный налоговый период
	 * @param taxPeriodId идентификатор отчётного периода
	 * @return список всех отчётных периодов
	 */
	@Select("select * from report_period where tax_period_id = #{taxPeriodId} order by ord")
	@ResultMap("reportPeriodMap")
	List<ReportPeriod> listByTaxPeriod(@Param("taxPeriodId")int taxPeriodId);
}
