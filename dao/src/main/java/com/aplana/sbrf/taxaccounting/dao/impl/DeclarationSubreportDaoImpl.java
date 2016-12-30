package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportDao;
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
public class DeclarationSubreportDaoImpl extends AbstractDao implements DeclarationSubreportDao {

	private static final Log LOG = LogFactory.getLog(DeclarationSubreportDaoImpl.class);

    @Autowired
    private BDUtils bdUtils;

    @Autowired
    private DeclarationSubreportParamDao declarationSubreportParamDao;

    private class DeclarationSubreportMapper implements RowMapper<DeclarationSubreport> {
		@Override
        public DeclarationSubreport mapRow(ResultSet rs, int index) throws SQLException {
			final DeclarationSubreport result = new DeclarationSubreport();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setAlias(rs.getString("alias"));
            result.setName(rs.getString("name"));
            result.setOrder(SqlUtils.getInteger(rs, "ord"));
            result.setBlobDataId(rs.getString("blob_data_id"));
            result.setDeclarationSubreportParams(declarationSubreportParamDao.getDeclarationSubreportParams(result.getId()));
            return result;
		}
	}

	@Override
    public List<DeclarationSubreport> getDeclarationSubreports(int declarationTemplateId) {
		return getJdbcTemplate().query(
				"SELECT id, declaration_template_id, name, alias, ord, blob_data_id " +
				"FROM declaration_subreport " +
				"WHERE declaration_template_id = ? " +
				"ORDER BY ord",
			new Object[] { declarationTemplateId },
			new int[] { Types.NUMERIC },
			new DeclarationSubreportMapper()
		);
	}

    @Override
    public DeclarationSubreport getSubreportByAlias(int declarationTemplateId, String alias) {
        return getJdbcTemplate().queryForObject(
                "SELECT id, declaration_template_id, name, alias, ord, blob_data_id " +
                        "FROM declaration_subreport " +
                        "WHERE declaration_template_id = ? and alias = ?",
                new Object[] { declarationTemplateId, alias },
                new int[] { Types.NUMERIC, Types.VARCHAR },
                new DeclarationSubreportMapper()
        );
    }

	@Override
	public void updateDeclarationSubreports(final DeclarationTemplate declarationTemplate) {
		final int declarationTemplateId = declarationTemplate.getId();

		JdbcTemplate jt = getJdbcTemplate();

		final Set<Long> removedSubreports = new HashSet<Long>(jt.queryForList(
			"SELECT id FROM declaration_subreport WHERE declaration_template_id = ?",
			new Object[] { declarationTemplateId },
			new int[] { Types.NUMERIC },
            Long.class
		));

		List<DeclarationSubreport> newSubreports =  new ArrayList<DeclarationSubreport>();
		List<DeclarationSubreport> oldSubreports = new ArrayList<DeclarationSubreport>();

        List<DeclarationSubreport> subreports = declarationTemplate.getSubreports();

        OrderUtils.reorder(subreports);
        for (DeclarationSubreport subreport: subreports) {
			if (!removedSubreports.contains(subreport.getId())) {
                newSubreports.add(subreport);
			} else {
                oldSubreports.add(subreport);
                removedSubreports.remove(subreport.getId());
			}
		}

		if(!removedSubreports.isEmpty()){
            deleteDeclarationSubreports(removedSubreports, declarationTemplate.getId());
		}
		if (!newSubreports.isEmpty()) {
            createDeclarationSubreports(newSubreports, declarationTemplate.getId());
		}
		if(!oldSubreports.isEmpty()){
            updateDeclarationSubreports(oldSubreports, declarationTemplate.getId());
		}
	}

    private List<Long> createDeclarationSubreports(final List<DeclarationSubreport> newSubreports, final int declarationTempldateId) {
        // Сгенерированый ключ -> реальный ключ в БД
        List<Long> genKeys = bdUtils.getNextIds(BDUtils.Sequence.DECLARATION_SUBREPORT, (long) newSubreports.size());
        for (int i = 0; i<newSubreports.size(); i++) {
            newSubreports.get(i).setId(genKeys.get(i).intValue());
        }
        getJdbcTemplate().batchUpdate(
                "INSERT INTO declaration_subreport (id, declaration_template_id, name, alias, ord, blob_data_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        DeclarationSubreport subreport = newSubreports.get(index);
                        ps.setLong(1, subreport.getId());
                        ps.setInt(2, declarationTempldateId);
                        ps.setString(3, subreport.getName());
                        ps.setString(4, subreport.getAlias());
                        ps.setInt(5, subreport.getOrder());
                        ps.setString(6, subreport.getBlobDataId());
                    }

                    @Override
                    public int getBatchSize() {
                        return newSubreports.size();
                    }
                }
        );

        return genKeys;
    }

    /**
     * Удаляем столбцы
     * @return возвращаем идентификаторы колонок удаленных
     */
    private void deleteDeclarationSubreports(final Set<Long> removedColumns, final int declarationTempldateId) {
        try {
            getJdbcTemplate().batchUpdate(
                    "DELETE FROM declaration_subreport WHERE id = ? AND declaration_template_id = ?",
                    new BatchPreparedStatementSetter() {

						Iterator<Long> iterator = removedColumns.iterator();

                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            Long id = iterator.next();
                            ps.setLong(1, id);
                            ps.setInt(2, declarationTempldateId);
                        }

                        @Override
                        public int getBatchSize() {
                            return removedColumns.size();
                        }
                    }
            );
        } catch (DataIntegrityViolationException e){
			LOG.error("", e);
            throw new DaoException("Обнаружено использование колонки", e);
        }
    }

    private Collection<Long> updateDeclarationSubreports(final List<DeclarationSubreport> oldSubreports, final int declarationTempldateId){
        getJdbcTemplate().batchUpdate(
                "UPDATE declaration_subreport SET name = ?, alias = ?, ord = ?, blob_data_id = ? " +
                        "WHERE id = ? and declaration_template_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        DeclarationSubreport subreport = oldSubreports.get(index);
                        ps.setString(1, subreport.getName());
                        ps.setString(2, subreport.getAlias());
                        ps.setInt(3, subreport.getOrder());
                        ps.setString(4, subreport.getBlobDataId());

                        ps.setLong(5, subreport.getId());
                        ps.setInt(6, declarationTempldateId);
                    }

                    @Override
                    public int getBatchSize() {
                        return oldSubreports.size();
                    }
                }
        );

        return null;
    }

    @Override
    public DeclarationSubreport getSubreportById(int id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, declaration_template_id, name, alias, ord, blob_data_id " +
                    "FROM declaration_subreport " +
                    "WHERE id = ?",
                    new Object[] { id },
                    new int[] { Types.NUMERIC },
                    new DeclarationSubreportMapper());
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}