package com.aplana.sbrf.taxaccounting.dao.mapper.typehandler;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Мапирует {@link ReportPeriod отчетные периоды} на числовые столбцы в базе
 * @author srybakov
 */
@MappedTypes(ReportPeriod.class)
public class ReportPeriodTypeHandler implements TypeHandler<ReportPeriod> {

	private final static int FROM_CHAR_CODE = 0;

	@Override
	public void setParameter(PreparedStatement preparedStatement, int i, ReportPeriod reportPeriod, JdbcType jdbcType) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReportPeriod getResult(ResultSet resultSet, String columnName) throws SQLException {
		Object value = resultSet.getObject(columnName);
		if (value == null) {
			return null;
		} else {
			ReportPeriod reportPeriod = new ReportPeriod();
			reportPeriod.setId(resultSet.getInt("id"));
			reportPeriod.setName(resultSet.getString("name"));
			reportPeriod.setTaxType(TaxType.fromCode(resultSet.getString("tax_type").charAt(FROM_CHAR_CODE)));
			reportPeriod.setActive(resultSet.getBoolean("is_active"));
			return reportPeriod;
		}
	}

	@Override
	public ReportPeriod getResult(ResultSet resultSet, int columnIndex) throws SQLException {
		Object value = resultSet.getObject(columnIndex);
		if (value == null) {
			return null;
		} else {
			ReportPeriod reportPeriod = new ReportPeriod();
			reportPeriod.setId(resultSet.getInt("id"));
			reportPeriod.setName(resultSet.getString("name"));
			reportPeriod.setTaxType(TaxType.fromCode(resultSet.getString("tax_type").charAt(FROM_CHAR_CODE)));
			reportPeriod.setActive(resultSet.getBoolean("is_active"));
			return reportPeriod;
		}
	}

	@Override
	public ReportPeriod getResult(CallableStatement callableStatement, int i) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
