package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Вспомогательные методы для работы с SQL в DAO
 * 
 * @author srybakov
 */
// TODO (Marat Fayzullin 10.03.2013) оптимизировать бы операции работы со
// строками. Слишком много явной конкатенации. В циклах лишнего добавления ","
// можно избежать
// (Semyon Goryachkin 19.04.2013) а ещё помоему JDBC поддерживает работу со
// списками параметров
public final class SqlUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private SqlUtils() {
	}

	/**
	 * Функция преобразует список (например, содержащий элементы 1,2,3,4) в
	 * строку вида "(1,2,3,4)", которая бдует использоваться в SQL запросах вида
	 * "...where param in (1,2,3,4)";
	 */
	public static String transformToSqlInStatement(List<?> list) {
		StringBuffer stringBuffer = new StringBuffer(list.toString());
		return '(' + (stringBuffer.substring(1, stringBuffer.length() - 1)) + ')';
	}

	public static String transformFormStatesToSqlInStatement(
			List<WorkflowState> source) {
		StringBuffer result = new StringBuffer("");
		for (WorkflowState workflowState : source) {
			result.append(workflowState.getId()).append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}

	public static String transformTaxTypeToSqlInStatement(List<TaxType> source) {
		StringBuffer result = new StringBuffer("");
		for (TaxType taxType : source) {
			result.append('\'').append(taxType.getCode()).append('\'')
					.append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}

	public static String transformFormKindsToSqlInStatement(
			List<FormDataKind> source) {
		StringBuffer result = new StringBuffer("");
		for (FormDataKind formDataKind : source) {
			result.append(formDataKind.getId()).append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}

	public static int[] batchUpdate(JdbcTemplate jdbcTemplate,
			PreparedStatementCreator psc,
			final BatchPreparedStatementSetter pss,
			final KeyHolder generatedKeyHolder) throws DataAccessException {

		return jdbcTemplate.execute(psc,
				new PreparedStatementCallback<int[]>() {
					public int[] doInPreparedStatement(PreparedStatement ps)
							throws SQLException {
						try {
							int batchSize = pss.getBatchSize();
							InterruptibleBatchPreparedStatementSetter ipss = (pss instanceof InterruptibleBatchPreparedStatementSetter ? (InterruptibleBatchPreparedStatementSetter) pss
									: null);
							List<Map<String, Object>> generatedKeys = generatedKeyHolder
									.getKeyList();
							generatedKeys.clear();
							/*
							 * for (int i = 0; i < batchSize; i++) {
							 * pss.setValues(ps, i); if (ipss != null &&
							 * ipss.isBatchExhausted(i)) { break; }
							 * ps.addBatch(); } int[] result =
							 * ps.executeBatch();
							 * 
							 * 
							 * ResultSet keys = ps.getGeneratedKeys(); if (keys
							 * != null) { try {
							 * RowMapperResultSetExtractor<Map<String, Object>>
							 * rse = new RowMapperResultSetExtractor<Map<String,
							 * Object>>( new ColumnMapRowMapper(), 1);
							 * generatedKeys.addAll(rse .extractData(keys)); }
							 * finally { JdbcUtils.closeResultSet(keys); } }
							 * return result;
							 */
							
							// Oracle не поддерживает получение ключей при использовании батч операции
							// http://stackoverflow.com/questions/9065894/jdbc-preparedstatement-batch-update-and-generated-keys
							
							List<Integer> rowsAffected = new ArrayList<Integer>();
							for (int i = 0; i < batchSize; i++) {
								pss.setValues(ps, i);
								if (ipss != null && ipss.isBatchExhausted(i)) {
									break;
								}
								rowsAffected.add(ps.executeUpdate());

								ResultSet keys = ps.getGeneratedKeys();
								if (keys != null) {
									try {
										RowMapperResultSetExtractor<Map<String, Object>> rse = new RowMapperResultSetExtractor<Map<String, Object>>(
												new ColumnMapRowMapper(), 1);
										generatedKeys.addAll(rse
												.extractData(keys));
									} finally {
										JdbcUtils.closeResultSet(keys);
									}
								}

							}
							int[] rowsAffectedArray = new int[rowsAffected
									.size()];
							for (int i = 0; i < rowsAffectedArray.length; i++) {
								rowsAffectedArray[i] = rowsAffected.get(i);
							}
							return rowsAffectedArray;

						} finally {
							if (pss instanceof ParameterDisposer) {
								((ParameterDisposer) pss).cleanupParameters();
							}
						}
					}
				});
	}

}
