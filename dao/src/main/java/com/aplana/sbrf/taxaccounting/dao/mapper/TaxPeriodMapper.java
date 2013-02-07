package com.aplana.sbrf.taxaccounting.dao.mapper;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface TaxPeriodMapper {
	/**
	 * Получить информацию по налоговому периоду
	 * @param id идентификатор периода
	 * @return объект, представляющий налоговый период или null, если такого периода нет
	 */
	@Select("select * from tax_period where id = #{taxPeriodId}")
	@ResultMap("taxPeriodMap")
	TaxPeriod get(@Param("taxPeriodId")int id);

	/**
	 * Получить список всех налоговых периодов
	 * @return список налоговых периодов
	 */
	@Select("select * from tax_period where tax_type = #{taxTypeCode}")
	@ResultMap("taxPeriodMap")
	List<TaxPeriod> listByTaxType(@Param("taxTypeCode")char taxTypeCode);
}