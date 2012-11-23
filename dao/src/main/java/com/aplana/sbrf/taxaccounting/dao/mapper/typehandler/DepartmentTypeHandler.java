package com.aplana.sbrf.taxaccounting.dao.mapper.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;

/**
 * Мапирует {@link DepartmentType типы подразделений} на числовые столбцы в базе
 * В качестве числового представления типа используется значения поля {@link DepartmentType#getCode()}
 * @author dsultanbekov
 */
@MappedTypes(DepartmentType.class)
public class DepartmentTypeHandler extends BaseTypeHandler<DepartmentType> {

	@Override
	public DepartmentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		if (rs.getObject(columnName) != null) {
			return DepartmentType.fromCode(rs.getInt(columnName));
		} else {
			return null;
		}
	}

	@Override
	public DepartmentType getNullableResult(ResultSet rs, int colIndex) throws SQLException {
		if (rs.getObject(colIndex) != null) {
			return DepartmentType.fromCode(rs.getInt(colIndex));
		} else {
			return null;
		}
	}

	@Override
	public DepartmentType getNullableResult(CallableStatement stmt, int index) throws SQLException {
		if (stmt.getObject(index) != null) {
			return DepartmentType.fromCode(stmt.getInt(index));
		} else {
			return null;
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement stmt, int index, DepartmentType value, JdbcType jdbcType) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
