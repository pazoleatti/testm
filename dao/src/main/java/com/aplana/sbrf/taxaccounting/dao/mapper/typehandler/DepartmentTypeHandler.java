package com.aplana.sbrf.taxaccounting.dao.mapper.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;

/**
 * Мапирует {@link DepartmentType типы подразделений} на числовые столбцы в базе
 * В качестве числового представления типа используется значения поля {@link DepartmentType#getCode()}
 * @author dsultanbekov
 */
@MappedTypes(DepartmentType.class)
public class DepartmentTypeHandler implements TypeHandler<DepartmentType> {
	@Override
	public DepartmentType getResult(ResultSet rs, String colName) throws SQLException {
		Object value = rs.getObject(colName);
		if (value == null) {
			return null;
		} else {
			return DepartmentType.fromCode(rs.getInt(colName));
		}
	}

	@Override
	public DepartmentType getResult(ResultSet rs, int index) throws SQLException {
		Object value = rs.getObject(index);
		if (value == null) {
			return null;
		} else {
			return DepartmentType.fromCode(rs.getInt(index));
		}
	}

	@Override
	public DepartmentType getResult(CallableStatement stmt, int index) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParameter(PreparedStatement stmt, int index, DepartmentType value, JdbcType type) throws SQLException {
		if (value == null) {
			stmt.setNull(index, Types.NUMERIC);	
		} else {
			stmt.setInt(index, value.getCode());
		}
	}
}
