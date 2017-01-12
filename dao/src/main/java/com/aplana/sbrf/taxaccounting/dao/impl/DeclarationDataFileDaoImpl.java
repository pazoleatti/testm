package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с информацией о {@link com.aplana.sbrf.taxaccounting.model.DeclarationDataFile файлах НФ(declaration)}
 */
@Repository
public class DeclarationDataFileDaoImpl extends AbstractDao implements DeclarationDataFileDao {

	private final static class DeclarationDataFilesMapper implements RowMapper<DeclarationDataFile> {
		@Override
        public DeclarationDataFile mapRow(ResultSet rs, int index) throws SQLException {
			final DeclarationDataFile result = new DeclarationDataFile();
			result.setDeclarationDataId(rs.getLong("declaration_data_id"));
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
	public List<DeclarationDataFile> getFiles(long declarationDataId) {
		return getJdbcTemplate().query(
				"select declaration_data_id, blob_data_id, user_name, user_department_name, note, bd.creation_date file_creation_date, bd.name file_name " +
                    "from declaration_data_file " +
                    "left join blob_data bd on bd.id=declaration_data_file.blob_data_id " +
                    "where declaration_data_id = ?",
				new Object[]{declarationDataId},
				new int[]{Types.NUMERIC},
				new DeclarationDataFilesMapper()
		);

	}

	@Override
	public void saveFiles(final long declarationDataId, List<DeclarationDataFile> files) {
        final List<DeclarationDataFile> newFiles = new LinkedList<DeclarationDataFile>();
        final List<DeclarationDataFile> oldFiles = new LinkedList<DeclarationDataFile>();
        final Set<String> removedFiles = new HashSet<String>(getJdbcTemplate().queryForList(
                "select blob_data_id from declaration_data_file where declaration_data_id = ?",
                new Object[]{declarationDataId},
                new int[]{Types.NUMERIC},
                String.class
        ));

        for (DeclarationDataFile file : files) {
            if (file.getDeclarationDataId() == 0) {
                newFiles.add(file);
            } else {
                oldFiles.add(file);
                removedFiles.remove(file.getUuid());
            }
        }

        if(!removedFiles.isEmpty()){
            getJdbcTemplate().batchUpdate(
                    "delete from declaration_data_file where blob_data_id = ?",
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
                    "insert into declaration_data_file (declaration_data_id, blob_data_id, user_name, user_department_name, note) " +
                            "values (?, ?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            DeclarationDataFile file = newFiles.get(index);
                            ps.setLong(1, declarationDataId);
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
                    "update declaration_data_file set note = ? " +
                            "where blob_data_id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            DeclarationDataFile file = oldFiles.get(index);
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
