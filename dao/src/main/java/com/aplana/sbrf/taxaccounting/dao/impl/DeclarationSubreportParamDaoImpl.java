package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportParamDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.OrderUtils;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
public class DeclarationSubreportParamDaoImpl extends AbstractDao implements DeclarationSubreportParamDao {

	private static final Log LOG = LogFactory.getLog(DeclarationSubreportParamDaoImpl.class);

    @Autowired
    private BDUtils bdUtils;

	private class DeclarationSubreportParamMapper implements RowMapper<DeclarationSubreportParam> {
		@Override
        public DeclarationSubreportParam mapRow(ResultSet rs, int index) throws SQLException {
			final DeclarationSubreportParam result = new DeclarationSubreportParam();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setAlias(rs.getString("alias"));
            result.setName(rs.getString("name"));
            result.setOrder(SqlUtils.getInteger(rs, "ord"));
            result.setDeclarationSubreportId(rs.getLong("declaration_subreport_id"));
            result.setRequired(rs.getBoolean("required"));
            String type = String.valueOf(rs.getString("type"));
            if ("D".equals(type)) {
                result.setType(DeclarationSubreportParamType.DATE);
            } else if ("N".equals(type)) {
                result.setType(DeclarationSubreportParamType.NUMBER);
            } else if ("R".equals(type)) {
                result.setType(DeclarationSubreportParamType.REFBOOK);
                result.setFilter(rs.getString("filter"));
                result.setRefBookAttributeId(rs.getLong("attribute_id"));
            } else if ("S".equals(type)) {
                result.setType(DeclarationSubreportParamType.STRING);
            } else {
                throw new IllegalArgumentException("Unknown param type: " + type);
            }

			return result;
		}
	}

	@Override
    public List<DeclarationSubreportParam> getDeclarationSubreportParams(long declarationSubreportId) {
		return getJdbcTemplate().query(
				"SELECT id, declaration_subreport_id, name, alias, ord, filter, attribute_id, required, type " +
				"FROM declaration_subreport_params " +
				"WHERE declaration_subreport_id = ? " +
				"ORDER BY ord",
			new Object[] { declarationSubreportId },
			new int[] { Types.NUMERIC },
			new DeclarationSubreportParamMapper()
		);
	}

    @Override
    public DeclarationSubreportParam getSubreportParamByAlias(long declarationSubreportId, String alias) {
        return getJdbcTemplate().queryForObject(
                "SELECT id, declaration_subreport_id, type, name, alias, ord, filter, attribute_id, required " +
                        "FROM declaration_subreport_params " +
                        "WHERE declaration_subreport_id = ? and alias = ?",
                new Object[] { declarationSubreportId, alias },
                new int[] { Types.NUMERIC, Types.VARCHAR },
                new DeclarationSubreportParamMapper()
        );
    }

	@Override
	public void updateDeclarationSubreports(final DeclarationTemplate declarationTemplate) {
		final int declarationTemplateId = declarationTemplate.getId();

		JdbcTemplate jt = getJdbcTemplate();

        for(DeclarationSubreport declarationSubreport: declarationTemplate.getSubreports()) {
            final Set<Long> removedSubreportParams = new HashSet<Long>(jt.queryForList(
                    "SELECT id FROM declaration_subreport_params WHERE declaration_subreport_id = ?",
                    new Object[]{declarationSubreport.getId()},
                    new int[]{Types.NUMERIC},
                    Long.class
            ));

            List<DeclarationSubreportParam> newSubreportParams = new ArrayList<DeclarationSubreportParam>();
            List<DeclarationSubreportParam> oldSubreportParams = new ArrayList<DeclarationSubreportParam>();
            List<DeclarationSubreportParam> subreportParams = declarationSubreport.getDeclarationSubreportParams();

            OrderUtils.reorder(subreportParams);
            for (DeclarationSubreportParam subreportParam : subreportParams) {
                if (!removedSubreportParams.contains(subreportParam.getId())) {
                    newSubreportParams.add(subreportParam);
                } else {
                    oldSubreportParams.add(subreportParam);
                    removedSubreportParams.remove(subreportParam.getId());
                }
            }

            if (!removedSubreportParams.isEmpty()) {
                deleteDeclarationSubreports(removedSubreportParams, declarationSubreport.getId());
            }
            if (!newSubreportParams.isEmpty()) {
                createDeclarationSubreports(newSubreportParams, declarationSubreport.getId());
            }
            if (!oldSubreportParams.isEmpty()) {
                updateDeclarationSubreports(oldSubreportParams, declarationSubreport.getId());
            }
        }
	}

    private List<Long> createDeclarationSubreports(final List<DeclarationSubreportParam> newSubreportParamss, final long declarationSubreportId) {
        // Сгенерированый ключ -> реальный ключ в БД
        List<Long> genKeys = bdUtils.getNextIds(BDUtils.Sequence.DECLARATION_SUBREPORT, (long) newSubreportParamss.size());
        for (int i = 0; i<newSubreportParamss.size(); i++) {
            newSubreportParamss.get(i).setId(genKeys.get(i).intValue());
        }
        getJdbcTemplate().batchUpdate(
                "INSERT INTO declaration_subreport_params (id, type, declaration_subreport_id, name, alias, ord, filter, attribute_id, required) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        DeclarationSubreportParam subreportParam = newSubreportParamss.get(index);
                        ps.setLong(1, subreportParam.getId());
                        ps.setString(2, String.valueOf(subreportParam.getType().getCode()));
                        ps.setLong(3, declarationSubreportId);
                        ps.setString(4, subreportParam.getName());
                        ps.setString(5, subreportParam.getAlias());
                        ps.setInt(6, subreportParam.getOrder());
                        ps.setString(7, subreportParam.getFilter());
                        if (subreportParam.getRefBookAttributeId() != null) {
                            ps.setLong(8, subreportParam.getRefBookAttributeId());
                        } else {
                            ps.setNull(8, Types.INTEGER);
                        }
                        ps.setBoolean(9, subreportParam.isRequired());
                    }

                    @Override
                    public int getBatchSize() {
                        return newSubreportParamss.size();
                    }
                }
        );

        return genKeys;
    }

    /**
     * Удаляем столбцы
     * @return возвращаем идентификаторы колонок удаленных
     */
    private void deleteDeclarationSubreports(final Set<Long> removedSubreportParams, final long declarationSubreportId) {
        try {
            getJdbcTemplate().batchUpdate(
                    "DELETE FROM declaration_subreport_params WHERE id = ? AND declaration_subreport_id = ?",
                    new BatchPreparedStatementSetter() {

						Iterator<Long> iterator = removedSubreportParams.iterator();

                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            Long id = iterator.next();
                            ps.setLong(1, id);
                            ps.setLong(2, declarationSubreportId);
                        }

                        @Override
                        public int getBatchSize() {
                            return removedSubreportParams.size();
                        }
                    }
            );
        } catch (DataIntegrityViolationException e){
			LOG.error("", e);
            throw new DaoException("Обнаружено использование колонки", e);
        }
    }

    private Collection<Long> updateDeclarationSubreports(final List<DeclarationSubreportParam> oldSubreportParams, final long declarationSubreportId){
        getJdbcTemplate().batchUpdate(
                "UPDATE declaration_subreport_params SET type = ?, name = ?, alias = ?, ord = ?, filter = ?, attribute_id = ?, required =? " +
                        "WHERE id = ? and declaration_subreport_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        DeclarationSubreportParam subreportParam = oldSubreportParams.get(index);
                        ps.setString(1, String.valueOf(subreportParam.getType().getCode()));
                        ps.setString(2, subreportParam.getName());
                        ps.setString(3, subreportParam.getAlias());
                        ps.setInt(4, subreportParam.getOrder());
                        ps.setString(5, subreportParam.getFilter());
                        if (subreportParam.getRefBookAttributeId() != null) {
                            ps.setLong(6, subreportParam.getRefBookAttributeId());
                        } else {
                            ps.setNull(6, Types.INTEGER);
                        }
                        ps.setBoolean(7, subreportParam.isRequired());

                        ps.setLong(8, subreportParam.getId());
                        ps.setLong(9, declarationSubreportId);
                    }

                    @Override
                    public int getBatchSize() {
                        return oldSubreportParams.size();
                    }
                }
        );

        return null;
    }

    @Override
    public DeclarationSubreportParam getSubreportParamById(long id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, declaration_subreport_id, type, name, alias, ord, filter, attribute_id, required " +
                    "FROM declaration_subreport_params " +
                    "WHERE id = ?",
                    new Object[] { id },
                    new int[] { Types.NUMERIC },
                    new DeclarationSubreportParamMapper());
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}