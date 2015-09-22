package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataFilesDao;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с информацией о {@link com.aplana.sbrf.taxaccounting.model.FormDataFile файлах НФ}
 */
@Repository
public class FormDataFilesDaoImpl extends AbstractDao implements FormDataFilesDao {

	private final static class FormDataFilesMapper implements RowMapper<FormDataFile> {
		@Override
        public FormDataFile mapRow(ResultSet rs, int index) throws SQLException {
			final FormDataFile result = new FormDataFile();
			result.setFormDataId(rs.getLong("form_data_id"));
            result.setUuid(rs.getString("blob_data_id"));
            result.setFileName(rs.getString("file_name"));
            result.setDate(new Date(rs.getTimestamp("file_creation_date").getTime()));
            result.setUserName(rs.getString("user_name"));
            result.setUserDepartmentName(rs.getString("user_department_name"));
            result.setNote(rs.getString("note"));
            return result;
		}
	}

	@Override
	public List<FormDataFile> getFiles(long formDataId) {
		return getJdbcTemplate().query(
				"select form_data_id, blob_data_id, user_name, user_department_name, note, bd.creation_date file_creation_date, bd.name file_name " +
                    "from form_data_file " +
                    "left join blob_data bd on bd.id=form_data_file.blob_data_id " +
                    "where form_data_id = ? " +
                    "order by file_creation_date desc",
				new Object[]{formDataId},
				new int[]{Types.NUMERIC},
				new FormDataFilesMapper()
		);

	}

	@Override
	public void saveFiles(final long formDataId, List<FormDataFile> files) {
        final List<FormDataFile> newFiles = new LinkedList<FormDataFile>();
        final List<FormDataFile> oldFiles = new LinkedList<FormDataFile>();
        final Set<String> removedFiles = new HashSet<String>(getJdbcTemplate().queryForList(
                "select blob_data_id from form_data_file where form_data_id = ?",
                new Object[]{formDataId},
                new int[]{Types.NUMERIC},
                String.class
        ));

        for (FormDataFile file : files) {
            if (file.getFormDataId() == 0) {
                newFiles.add(file);
            } else {
                oldFiles.add(file);
                removedFiles.remove(file.getUuid());
            }
        }

        if(!removedFiles.isEmpty()){
            getJdbcTemplate().batchUpdate(
                    "delete from form_data_file where blob_data_id = ?",
                    new BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            ps.setString(1, iterator.next());
                        }

                        @Override
                        public int getBatchSize() {
                            return removedFiles.size();
                        }

                        private Iterator<String> iterator = removedFiles.iterator();
                    }
            );
        }

        // create new
        if (!newFiles.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "insert into form_data_file (form_data_id, blob_data_id, user_name, user_department_name, note, attachment_date) " +
                            "values (?, ?, ?, ?, ?, sysdate)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            FormDataFile file = newFiles.get(index);
                            ps.setLong(1, formDataId);
                            ps.setString(2, file.getUuid());
                            ps.setString(3, file.getUserName());
                            ps.setString(4, file.getUserDepartmentName());
                            ps.setString(5, file.getNote());
                        }

                        @Override
                        public int getBatchSize() {
                            return newFiles.size();
                        }
                    }
            );
        }
        // update old
        if (!oldFiles.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "update form_data_file set note = ? " +
                            "where blob_data_id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            FormDataFile file = oldFiles.get(index);
                            ps.setString(1, file.getNote());
                            ps.setString(2, file.getUuid());
                        }

                        @Override
                        public int getBatchSize() {
                            return oldFiles.size();
                        }
                    }
            );
        }
	}

}
