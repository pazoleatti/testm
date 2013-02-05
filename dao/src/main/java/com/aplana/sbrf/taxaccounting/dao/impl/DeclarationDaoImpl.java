package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Declaration;

/**
 * Реализация Dao для работы с декларациями
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDaoImpl extends AbstractDao implements DeclarationDao {
	
	private static final class DeclarationRowMapper implements RowMapper<Declaration> {
		@Override
		public Declaration mapRow(ResultSet rs, int index) throws SQLException {
			Declaration d = new Declaration();
			d.setId(rs.getLong("id"));
			d.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
			d.setDepartmentId(rs.getInt("department_id"));			
			d.setReportPeriodId(rs.getInt("report_period_id"));			
			d.setAccepted(rs.getBoolean("is_accepted"));
			return d;
		}
	}
	
	@Override
	public Declaration get(long declarationId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration where id = ?",
				new Object[] { declarationId },
				new DeclarationRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationId);
		}
	}

	@Override
	public String getXmlData(long declarationId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select data from declaration where id = ?",
				new Object[] { declarationId },
				String.class
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationId);
		}
	}

	@Override
	public void setXmlData(long declarationId, String xmlData) {
		int count = getJdbcTemplate().update(
			"update declaration set data = ? where id = ?",
			new Object[] {
				xmlData,
				declarationId
			},
			new int[] {
				Types.VARCHAR,
				Types.NUMERIC
			}
		);
		if (count == 0) {
			throw new DaoException("Не удалось сохранить данные в формате законодателя для декларации с id = %d, так как она не существует.");
		}
	}

	@Override
	public void delete(long declarationId) {
		int count = getJdbcTemplate().update(
			"delete from declaration where id = ?",
			declarationId
		);
		if (count == 0) {
			throw new DaoException("Не удалось удалить декларацию с id = %d, так как она не существует", declarationId);
		}
	}

	@Override
	public long saveNew(Declaration declaration) {
		JdbcTemplate jt = getJdbcTemplate();
		
		Long id = declaration.getId();
		if (id != null) {
			throw new DaoException("Произведена попытка перезаписать уже сохранённую декларацию");
		}
		id = generateId("seq_declaration", Long.class);
		jt.update(
			"insert into declaration (id, declaration_template_id, report_period_id, department_id, is_accepted) values (?, ?, ?, ?, ?)",
			id,
			declaration.getDeclarationTemplateId(),
			declaration.getReportPeriodId(),
			declaration.getDepartmentId(),
			declaration.isAccepted() ? 1 : 0
		);
		declaration.setId(id);
		return id.longValue();
	}

	@Override
	public void setAccepted(long declarationId, boolean accepted) {
		int count = getJdbcTemplate().update(
			"update declaration set is_accepted = ? where id = ?",
			accepted,
			declarationId
		);
		if (count == 0) {
			throw new DaoException("Не удалось изменить статус декларации с id = %d, так как она не существует.", declarationId);
		}		
	}
}
