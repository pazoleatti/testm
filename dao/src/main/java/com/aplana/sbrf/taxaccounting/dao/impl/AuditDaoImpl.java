package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.List;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private DepartmentDao departmentDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
	@Autowired
	private DeclarationTypeDao declarationTypeDao;
	@Autowired
	private FormTypeDao formTypeDao;

	private static final String dbDateFormat = "YYYYMMDD HH24:MI:SS";
	private static final String dateFormat = "yyyyMMdd HH:mm:ss";
	private static final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

	@Override
	public PagingResult<LogSystemSearchResultItem> getLogs(LogSystemFilter filter) {
		StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from ( select ");
        sql.append("ls.id, ");
        sql.append("ls.log_date, ");
        sql.append("ls.ip, ");
        sql.append("ls.event_id, ");
        sql.append("ls.user_id, ");
        sql.append("ls.roles, ");
        sql.append("ls.department_id, ");
        sql.append("ls.report_period_id, ");
        sql.append("ls.declaration_type_id, ");
        sql.append("ls.form_type_id, ");
        sql.append("ls.form_kind_id, ");
        sql.append("ls.note, ");
        sql.append("ls.user_department_id ");

        sql.append(" from log_system ls ");

        sql.append("left join department dep on ls.department_id=dep.\"ID\" ");
        sql.append("left join form_type ft on ls.form_type_id=ft.\"ID\" ");
        sql.append("left join declaration_type dt on ls.declaration_type_id=dt.\"ID\" ");
        sql.append("left join sec_user su on ls.user_id=su.\"ID\" ");
        sql.append("left join REPORT_PERIOD rp on ls.report_period_id=rp.\"ID\" ");
        sql.append("left join TAX_PERIOD tp on rp.tax_period_id=tp.\"ID\" ");

		appendSelectWhereClause(sql, filter);

		sql.append(" order by ls.id desc,");
		sql.append(" tp.start_date desc,");
		sql.append(" rp.months desc,");
		sql.append(" dep.name asc,");
		sql.append(" dt.name asc,");
		sql.append(" ft.name asc,");
		sql.append(" ls.form_kind_id asc,");
		sql.append(" su.name asc ");

		sql.append(") dat) ordDat");
        List<LogSystemSearchResultItem> records;
        if(filter.getCountOfRecords() != 0){
            sql.append(" where ordDat.rn between ? and ?")
                    .append(" order by ordDat.rn");
            records = getJdbcTemplate().query(
                    sql.toString(),
                    new Object[] {
                            filter.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
                            filter.getStartIndex() + filter.getCountOfRecords()
                    },
                    new int[] {
                            Types.NUMERIC,
                            Types.NUMERIC
                    },
                    new AuditRowMapper()
            );
        }else{
            sql.append(" order by ordDat.rn");
            records = getJdbcTemplate().query(sql.toString(),
                    new AuditRowMapper());
        }

		PagingResult<LogSystemSearchResultItem> result = new PagingResult<LogSystemSearchResultItem>();
		result.setRecords(records);
		result.setTotalRecordCount(getCount(filter));

		return result;
	}

	@Override
	public void add(LogSystem logSystem) {
		JdbcTemplate jt = getJdbcTemplate();

		Long id = logSystem.getId();
		if (id == null) {
			id = generateId("seq_log_system", Long.class);
		}

		jt.update(
				"insert into log_system (id, log_date, ip, event_id, user_id, roles, department_id, report_period_id, " +
						"declaration_type_id, form_type_id, form_kind_id, note, user_department_id)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				logSystem.getLogDate(),
				logSystem.getIp(),
				logSystem.getEventId(),
				logSystem.getUserId(),
				logSystem.getRoles(),
				logSystem.getDepartmentId(),
				logSystem.getReportPeriodId(),
				logSystem.getDeclarationTypeId(),
				logSystem.getFormTypeId(),
				logSystem.getFormKindId(),
				logSystem.getNote(),
				logSystem.getUserDepartmentId()
		);
	}

	private void appendSelectWhereClause(StringBuilder sql, LogSystemFilter filter) {
		sql.append(" WHERE log_date BETWEEN TO_DATE('").append
				(formatter.format(filter.getFromSearchDate()))
				.append("', '").append(dbDateFormat).append("')").append(" AND TO_DATE('").append
				(formatter.format(filter.getToSearchDate()))
				.append("', '").append(dbDateFormat).append("')");

		if (filter.getUserId() != null) {
			sql.append(" AND user_id = ").append(filter.getUserId());
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
            sql.append(" AND report_period_id = ").append(filter.getReportPeriodIds().get(0));
			for (int i = 1; i < filter.getReportPeriodIds().size(); i++) {
				sql.append(" OR report_period_id = ").append(filter.getReportPeriodIds().get(i));
			}
		}

		if (filter.getFormKind() != null && filter.getFormKind().getId() != 0) {
			sql.append(" AND form_kind_id = ").append(filter.getFormKind().getId());
		}

		if (filter.getFormTypeId() != null) {
			sql.append(" AND form_type_id = ").append(filter.getFormTypeId());
		}

		if (filter.getDeclarationTypeId() != null) {
			sql.append(" AND declaration_type_id = ").append(filter.getDeclarationTypeId());
		}

        if (filter.getAuditFormTypeId() != null) {
            if (filter.getAuditFormTypeId().equals(AuditFormType.FORM_TYPE_TAX.getId())) {
                sql.append(" AND form_type_id is not null ");
            } else if (filter.getAuditFormTypeId().equals(AuditFormType.FORM_TYPE_DECLARATION.getId())) {
                sql.append(" AND declaration_type_id is not null ");
            }
        }

		if (filter.getDepartmentId() != null) {
            sql.append(" AND department_id = ").append(filter.getDepartmentId());
		}
	}

	private final class AuditRowMapper implements RowMapper<LogSystemSearchResultItem> {
		@Override
		public LogSystemSearchResultItem mapRow(ResultSet rs, int index) throws SQLException {
			LogSystemSearchResultItem log = new LogSystemSearchResultItem();
			log.setId(rs.getLong("id"));
			log.setLogDate(rs.getTimestamp("log_date"));
			log.setIp(rs.getString("ip"));
			log.setEvent(FormDataEvent.getByCode(rs.getInt("event_id")));
			log.setUser(userDao.getUser(rs.getInt("user_id")));
			log.setRoles(rs.getString("roles"));
			log.setDepartment(departmentDao.getDepartment(rs.getInt("department_id")));
			log.setReportPeriod(reportPeriodDao.get(rs.getInt("report_period_id")));
            if(rs.getInt("declaration_type_id") != 0)
			    log.setDeclarationType(declarationTypeDao.get(rs.getInt("declaration_type_id")));
            if(rs.getInt("form_type_id") != 0)
			    log.setFormType(formTypeDao.getType(rs.getInt("form_type_id")));
			if (rs.getInt("form_kind_id") != 0) {
				log.setFormKind(FormDataKind.fromId(rs.getInt("form_kind_id")));
			}
			log.setNote(rs.getString("note"));
			log.setUserDepartment(departmentDao.getDepartment(rs.getInt("user_department_id")));
			return log;
		}
	}

	private int getCount(LogSystemFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*) from log_system");
		appendSelectWhereClause(sql, filter);
		return getJdbcTemplate().queryForInt(sql.toString());
	}
}
