package com.aplana.sbrf.taxaccounting.dao.dictionary.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TransportTaxMapper {
	@Select("select name from okato where okato = #{okato}")
	String getRegionName(@Param("okato") String okato);
}
