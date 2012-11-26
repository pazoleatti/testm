package com.aplana.sbrf.taxaccounting.dao.mapper.typehandler;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Мапирует {@link TaxType типы подразделений} на числовые столбцы в базе
 * В качестве числового представления типа используется значения поля {@link TaxType#getCode()}
 * @author srybakov
 */
@MappedTypes(TaxType.class)
public class TaxTypeHandler extends BaseTypeHandler<TaxType> {

	private final static int FROM_CHAR_CODE = 0;

	@Override
	public TaxType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		if (rs.getObject(columnName) != null) {
			return TaxType.fromCode(rs.getString(columnName).charAt(FROM_CHAR_CODE));
		} else {
			return null;
		}
	}

	@Override
	public TaxType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		if (rs.getObject(columnIndex) != null) {
			return TaxType.fromCode(rs.getString(columnIndex).charAt(FROM_CHAR_CODE));
		} else {
			return null;
		}
	}

	@Override
	public TaxType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		if (cs.getObject(columnIndex) != null) {
			return TaxType.fromCode(cs.getString(columnIndex).charAt(FROM_CHAR_CODE));
		} else {
			return null;
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TaxType parameter, JdbcType jdbcType) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
