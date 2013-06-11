package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface ReportPeriodMapper {
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param periodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 */
	@Select("select * from report_period where id = #{periodId}")
	@ResultMap("reportPeriodMap")
	ReportPeriod get(@Param("periodId")int periodId);

	/**
	 * Получить объект текущего отчётного периода по виду налога. Поиск ведется только по обычным периодам, то есть
	 * периоды для ввода остатков исключены из поиска
	 * @param taxType код вида налога
	 * @return объект представляющий текущий отчётный период по заданному виду налога, или null, если такого периода нет (еще не открыли)
	 */
	@Select("select rp.* from report_period rp join tax_period tp on rp.tax_period_id = tp.id where " +
			"tp.tax_type = #{taxTypeCode} and rp.is_active = 1 and rp.is_balance_period = 0")
	@ResultMap("reportPeriodMap")
	ReportPeriod getCurrentPeriod(@Param("taxTypeCode")char taxType);

	/**
	 * Получить полный список отчётных периодов по виду налога
	 * @param taxTypeCode код вида налога
	 * @return список всех отчётных периодов
	 */
	@Select("select t1.* from report_period t1 join tax_period t2 on t1.tax_period_id = t2.id where t2.tax_type = #{taxTypeCode} ")
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

	/**
	 *
	 */
	@Select("select t1.* from report_period t1 join tax_period t2 on t1.tax_period_id = t2.id where t2.start_date >= #{from} and t2.end_date <= #{to}")
	@ResultMap("reportPeriodMap")
	List<ReportPeriod> getReportPeriodsFromTo(@Param("from")Date from, @Param("to")Date to);
}
