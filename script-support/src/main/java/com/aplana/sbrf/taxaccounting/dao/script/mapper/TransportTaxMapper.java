package com.aplana.sbrf.taxaccounting.dao.script.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TransportTaxMapper {
	/**
	 * В БД есть уникальность по коду типа транспортного средства.
	 */
	@Select("select name from transport_type_code where code = #{tsTypeCode}")
	String getTsTypeName(@Param("tsTypeCode") String tsTypeCode);
}
