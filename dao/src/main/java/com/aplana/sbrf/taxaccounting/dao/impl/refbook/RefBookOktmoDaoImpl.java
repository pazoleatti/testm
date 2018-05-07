package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

/**
 * Дао для работы со справочником ОКТМО без использования API справочников
 * @author dloshkarev
 */
@Repository
public class RefBookOktmoDaoImpl extends AbstractDao implements RefBookOktmoDao {
    private static final Log LOG = LogFactory.getLog(RefBookOktmoDaoImpl.class);

    private static final class OktmoMapper implements RowMapper<RefBookOktmo> {

        @Override
        public RefBookOktmo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookOktmo result = new RefBookOktmo();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            result.setSection(rs.getInt("razd"));
            return result;
        }
    }

    @Override
    public PagingResult<RefBookOktmo> fetchAll(String filter, PagingParams pagingParams) {
        StringBuilder baseSql =
                new StringBuilder("select o.* from REF_BOOK_OKTMO o\n" +
                        "join (select record_id, max(version) as maxVersion from REF_BOOK_OKTMO where status = 0 group by record_id) g on o.record_id = g.record_id and o.version = g.maxVersion\n");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!StringUtils.isBlank(filter)) {
            baseSql.append("where (lower(o.name) like :filter or lower(o.code) like :filter) ");
            params.addValue("filter", "%" + filter.toLowerCase() + "%");
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select * from ( ")
                .append(" select a.*, rownum rn from ( ")
                .append(baseSql);


        if (StringUtils.isNotBlank(pagingParams.getProperty()) && StringUtils.isNotBlank(pagingParams.getDirection())) {
            queryBuilder.append(new Formatter().format(" order by %s %s ",
                    FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()),
                    pagingParams.getDirection()).toString());
        }

        queryBuilder.append(") a) where rn > :startIndex and rowNum <= :count");

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());

        List<RefBookOktmo> result = getNamedParameterJdbcTemplate().query(queryBuilder.toString(),
                params,
                new OktmoMapper());

        int count = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSql.toString() + ")", params, Integer.class);
        return new PagingResult<>(result, count);
    }

    @Override
    public RefBookOktmo fetchByCode(String code, Date version) {
        String sql = "select o.* from REF_BOOK_OKTMO o\n" +
                "join (select record_id, max(version) as maxVersion from REF_BOOK_OKTMO where status = 0 and version <= :version group by record_id) g on o.record_id = g.record_id and o.version = g.maxVersion\n" +
                "where o.code = :code";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("version", version);
        params.addValue("code", code);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new OktmoMapper());
        } catch (DataAccessException e) {
            LOG.warn("Запись с кодом %s не найдена в справочнике ОКТМО");
            return null;
        }
    }
}
