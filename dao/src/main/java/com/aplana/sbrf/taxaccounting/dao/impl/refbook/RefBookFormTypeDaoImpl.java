package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class RefBookFormTypeDaoImpl extends AbstractDao implements RefBookFormTypeDao {

    @Override
    public List<RefBookFormType> fetchAll() {
        return getJdbcTemplate().query("select id, code, name from REF_BOOK_FORM_TYPE where id != -1", new RefBookFormTypeRowMapper());
    }

    @Override
    public RefBookFormType findOne(int id) {
        return getJdbcTemplate().queryForObject("select id, code,name from REF_BOOK_FORM_TYPE where id = ?",
                new Object[]{id},
                new RefBookFormTypeRowMapper()
        );

    }

    public static final class RefBookFormTypeRowMapper implements RowMapper<RefBookFormType> {
        private String prefix;

        RefBookFormTypeRowMapper() {
        }

        public RefBookFormTypeRowMapper(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public RefBookFormType mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookFormType refBookFormType = null;
            Long id = SqlUtils.getLong(resultSet, prefix == null ? "id" : prefix + "id");
            if (id != null) {
                refBookFormType = new RefBookFormType();
                refBookFormType.setId(id);
                refBookFormType.setCode(resultSet.getString(prefix == null ? "code" : prefix + "code"));
                refBookFormType.setName(resultSet.getString(prefix == null ? "name" : prefix + "name"));
            }

            return refBookFormType;
        }
    }
}
