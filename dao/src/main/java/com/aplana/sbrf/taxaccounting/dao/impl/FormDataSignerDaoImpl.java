package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSignerDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с информацией о {@link FormDataSigner подписантах} налоговых форм
 */
@Repository
public class FormDataSignerDaoImpl extends AbstractDao implements FormDataSignerDao {

	private final static class FormDataSignerMapper implements RowMapper<FormDataSigner> {
		public FormDataSigner mapRow(ResultSet rs, int index) throws SQLException {
			final FormDataSigner result = new FormDataSigner();
			result.setId(rs.getLong("id"));
			result.setName(rs.getString("name"));
			result.setPosition(rs.getString("position"));
			return result;
		}
	}

	@Override
	public List<FormDataSigner> getSigners(long formDataId) {
		return getJdbcTemplate().query(
				"select * from form_data_signer where form_data_id = ? order by ord asc",
				new Object[]{formDataId},
				new int[]{Types.NUMERIC},
				new FormDataSignerMapper()
		);

	}

	@Override
	public void saveSigners(final long formDataId, List<FormDataSigner> signers) {
		final List<FormDataSigner> newSigners = new LinkedList<FormDataSigner>();
		final List<FormDataSigner> oldSigners = new LinkedList<FormDataSigner>();
		final Set<Long> removedSigners = new HashSet<Long>(getJdbcTemplate().queryForList(
				"select id from form_data_signer where form_data_id = ?",
				new Object[]{formDataId},
				new int[]{Types.NUMERIC},
				Long.class
		));
		for (FormDataSigner signer : signers) {
			if (signer.getId() == 0) {
				newSigners.add(signer);
			} else {
				oldSigners.add(signer);
				removedSigners.remove(signer.getId());
			}
		}

		if(!removedSigners.isEmpty()){
			getJdbcTemplate().batchUpdate(
					"delete from form_data_signer where id = ?",
					new BatchPreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							ps.setLong(1, iterator.next());
						}

						@Override
						public int getBatchSize() {
							return removedSigners.size();
						}

						private Iterator<Long> iterator = removedSigners.iterator();
					}
			);
		}

		// create new
		if (!newSigners.isEmpty()) {
			getJdbcTemplate().batchUpdate(
					"insert into form_data_signer (id, form_data_id, name, position, ord) " +
							"values (seq_form_data_signer.nextval, ?, ?, ?, (select case when max(ord) is null then 0 else max(ord) end from " +
							"(select * from form_data_signer where form_data_id = ?))+1)",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							FormDataSigner signer = newSigners.get(index);
							ps.setLong(1, formDataId);
							ps.setString(2, signer.getName());
							ps.setString(3, signer.getPosition());
							ps.setLong(4, formDataId);
						}

						@Override
						public int getBatchSize() {
							return newSigners.size();
						}
					}
			);
		}
		// update old
		if (!oldSigners.isEmpty()) {
			getJdbcTemplate().batchUpdate(
				"update form_data_signer set form_data_id = ?, name = ?, position = ?" +
						"where id = ?",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						FormDataSigner signer = oldSigners.get(index);
						ps.setLong(1, formDataId);
						ps.setString(2, signer.getName());
						ps.setString(3, signer.getPosition());
						ps.setLong(4, signer.getId());
					}

					@Override
					public int getBatchSize() {
						return oldSigners.size();
					}
				}
			);
		}
	}

}
