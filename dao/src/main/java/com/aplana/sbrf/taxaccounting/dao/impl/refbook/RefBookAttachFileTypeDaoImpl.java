package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAttachFileTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Реализация дао для работы со справочником Категории прикрепленных файлов
 */
@Repository
public class RefBookAttachFileTypeDaoImpl extends AbstractDao implements RefBookAttachFileTypeDao {

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAttachFileType> fetchAll() {
        return getJdbcTemplate().query("select aft.id, aft.code, aft.name " +
                        "from ref_book_attach_file_type aft " +
                        "where aft.code > 0",
                new RefBookAttachFileTypeRowMapper());
    }

    private static final class RefBookAttachFileTypeRowMapper implements RowMapper<RefBookAttachFileType> {
        @Override
        public RefBookAttachFileType mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookAttachFileType refBookAttachFileType = new RefBookAttachFileType();
            refBookAttachFileType.setId(SqlUtils.getLong(resultSet, "id"));
            refBookAttachFileType.setCode(resultSet.getByte("code"));
            refBookAttachFileType.setName(resultSet.getString("name"));
            return refBookAttachFileType;
        }
    }
}