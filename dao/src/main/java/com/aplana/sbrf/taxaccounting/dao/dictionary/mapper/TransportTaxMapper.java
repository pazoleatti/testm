package com.aplana.sbrf.taxaccounting.dao.dictionary.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TransportTaxMapper {
	/**
	 * В БД есть уникальность по коду ОКАТО
	 */
	@Select("select name from transport_okato where okato = #{okato}")
	String getRegionName(@Param("okato")String okato);
	
	@Select({
		"select value from transport_tax_rate", 
		"where code = #{transportCode}",
		"and (min_age is null or min_age < #{age}) and (max_age is null or max_age >= #{age})",
		"and (min_power is null or min_power < #{power}) and (max_power is null or max_power >= #{power})"
	})
	List<Integer> getTransportTaxRate(@Param("transportCode")String transportCode, @Param("age")int age, @Param("power")int power);

	/**
	 * В БД есть уникальность по коду типа транспортного средства.
	 */
	@Select("select name from transport_type_code where code = #{tsTypeCode}")
	String getTsTypeName(@Param("tsTypeCode")String tsTypeCode);
}
