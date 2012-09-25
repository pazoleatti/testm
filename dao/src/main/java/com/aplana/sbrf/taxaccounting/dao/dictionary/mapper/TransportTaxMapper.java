package com.aplana.sbrf.taxaccounting.dao.dictionary.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TransportTaxMapper {
	@Select("select name from okato where okato = #{okato}")
	String getRegionName(@Param("okato")String okato);
	
	@Select({
		"select value from transport_tax_rate", 
		"where code = #{transportCode}",
		"and (min_age is null or min_age <= #{age}) and (max_age is null or max_age >= #{age})",
		"and (min_power is null or min_power <= #{power}) and (max_power is null or max_power >= #{power})"
	})
	Integer getTransportTaxRate(@Param("transportCode")String transportCode, @Param("age")int age, @Param("power")int power);
}
