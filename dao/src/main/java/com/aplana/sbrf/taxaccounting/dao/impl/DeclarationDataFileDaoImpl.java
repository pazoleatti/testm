package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
            result.setFileTypeName(rs.getString("file_type_name"));
            result.setFileTypeId(rs.getLong("file_type_id"));
            result.setFileKind(rs.getString("file_kind"));
            return result;
        }
    }

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private final static class DeclarationDataFileShortMapper implements RowMapper<DeclarationDataFile> {
        @Override
        public DeclarationDataFile mapRow(ResultSet rs, int index) throws SQLException {
            final DeclarationDataFile result = new DeclarationDataFile();
            result.setFileName(rs.getString("file_name"));
            result.setDate(new Date(rs.getTimestamp("file_creation_date").getTime()));
            return result;
        }
    }

    @Override
    public List<DeclarationDataFile> fetchByDeclarationDataId(long declarationDataId) {
        return getJdbcTemplate().query(
                "select " +
                        "declaration_data_id, blob_data_id, user_name, user_department_name, note, file_kind, " +
                        "bd.creation_date file_creation_date, bd.name file_name, ft.name file_type_name, ft.id file_type_id " +
                        "from declaration_data_file " +
                        "left join blob_data bd on (bd.id = declaration_data_file.blob_data_id) " +
                        "left join ref_book_attach_file_type ft on (ft.id = declaration_data_file.file_type_id) " +
                        "where declaration_data_id = ?",
                new Object[]{declarationDataId},
                new int[]{Types.NUMERIC},
                new DeclarationDataFilesMapper()
        );

    }

    @Override
    public void createOrUpdateList(final long declarationDataId, List<DeclarationDataFile> files) {
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

        if (!removedFiles.isEmpty()) {
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
                    "insert into declaration_data_file (declaration_data_id, blob_data_id, user_name, user_department_name, note, file_type_id, file_kind) " +
                            "values (?, ?, ?, ?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            DeclarationDataFile file = newFiles.get(index);
                            ps.setLong(1, declarationDataId);
                            ps.setString(2, file.getUuid());
                            ps.setString(3, file.getUserName());
                            ps.setString(4, file.getUserDepartmentName());
                            ps.setString(5, file.getNote());
                            ps.setLong(6, file.getFileTypeId());
                            ps.setString(7, file.getFileKind());
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
                    "update declaration_data_file set note = ?, file_type_id = ?, file_kind = ? " +
                            "where blob_data_id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            DeclarationDataFile file = oldFiles.get(index);
                            ps.setString(1, file.getNote());
                            ps.setLong(2, file.getFileTypeId());
                            ps.setString(3, file.getFileKind());
                            ps.setString(4, file.getUuid());
                        }

                        @Override
                        public int getBatchSize() {
                            return oldFiles.size();
                        }
                    }
            );
        }
    }

    @Override
    public void create(DeclarationDataFile file) {
        getJdbcTemplate().update(
                "insert into declaration_data_file (declaration_data_id, blob_data_id, user_name, user_department_name, note, file_type_id, file_kind) values (?, ?, ?, ?, ?, ?, ?)",
                file.getDeclarationDataId(),
                file.getUuid(),
                file.getUserName(),
                file.getUserDepartmentName(),
                file.getNote(),
                file.getFileTypeId(),
                file.getFileKind());
    }

    @Override
    public long deleteByDeclarationDataIdAndType(long declarationDataId, AttachFileType type) {
        String query = "delete from DECLARATION_DATA_FILE ddf " +
                "where ddf.DECLARATION_DATA_ID = :declarationDataId and ddf.FILE_TYPE_ID = :type";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);
        params.addValue("type", type.getId());
        return getNamedParameterJdbcTemplate().update(query, params);
    }

    @Override
    public DeclarationDataFile fetchWithMaxWeight(Long declarationDataId) {
        String sql =
                "select " +
                        "t.name file_name, t.creation_date file_creation_date " +
                        "from (  " +
                        "select " +
                        "bd.name, bd.creation_date, " +
                        "case when (lower(bd.name) like 'iv_%' or lower(bd.name) like 'uu_%') then 2 else 1 end weight " +
                        "from " +
                        "declaration_data_file ddf " +
                        "join declaration_data dd on (ddf.declaration_data_id = dd.id) " +
                        "join blob_data bd on ddf.blob_data_id = bd.id " +
                        "where " +
                        "dd.id = :declarationDataId and ( " +
                        "lower(bd.name) like 'prot_no_ndfl2%' " +
                        "or lower(bd.name) like 'прот_no_ndfl2%' " +
                        "or lower(bd.name) like 'reestr_no_ndfl2%' " +
                        "or lower(bd.name) like 'реестр_no_ndfl2%' " +
                        "or lower(bd.name) like 'kv_%' " +
                        "or lower(bd.name) like 'uo_%' " +
                        "or lower(bd.name) like 'iv_%' " +
                        "or lower(bd.name) like 'uu_%' " +
                        ") " +
                        "order by " +
                        "weight desc, " +
                        "bd.creation_date desc " +
                        ") t " +
                        "where rownum = 1 ";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);

        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new DeclarationDataFileShortMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<DeclarationDataFile> findAllByDeclarationIdAndType(Long declarationDataId, AttachFileType fileType) {
        String query = "select declaration_data_id, blob_data_id, user_name, user_department_name, note, file_kind, \n" +
                "  bd.creation_date file_creation_date, bd.name file_name, ft.name file_type_name, ft.id file_type_id\n" +
                "from declaration_data_file\n" +
                "join blob_data bd on bd.id = declaration_data_file.blob_data_id\n" +
                "join ref_book_attach_file_type ft on ft.id = declaration_data_file.file_type_id\n" +
                "where declaration_data_id = :declarationDataId and ft.code = :fileTypeCode";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);
        params.addValue("fileTypeCode", fileType.getCode());
        return getNamedParameterJdbcTemplate().query(query, params, new DeclarationDataFilesMapper());
    }

    @Override
    public void deleteTransportFileExcel(long declarationDataId) {
        String query = "delete from DECLARATION_DATA_FILE ddf " +
                "where ddf.DECLARATION_DATA_ID = :declarationDataId " +
                "and ddf.FILE_TYPE_ID = :type " +
                "and ddf.blob_data_id = (select id from blob_data bd where bd.id = ddf.blob_data_id and bd.name like '%.xlsx')";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);
        params.addValue("type", AttachFileType.TRANSPORT_FILE.getId());
        getNamedParameterJdbcTemplate().update(query, params);
    }

    @Override
    public boolean isExists(long declarationDataId, String blobId) {
        String sql = "select count(*) from DECLARATION_DATA_FILE where DECLARATION_DATA_ID = :declarationDataId and BLOB_DATA_ID = :blobId";
        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", declarationDataId);
        params.addValue("blobId", blobId);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class) > 0;
    }
}
