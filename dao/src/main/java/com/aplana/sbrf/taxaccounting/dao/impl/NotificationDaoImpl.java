package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository
public class NotificationDaoImpl extends AbstractDao implements NotificationDao {

    private class NotificationMapper implements RowMapper<Notification> {
        @Override
        public Notification mapRow(ResultSet rs, int index) throws SQLException {
            Notification notification = new Notification();
            notification.setId(rs.getInt("ID"));
            notification.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
            notification.setSenderDepartmentId(rs.getInt("SENDER_DEPARTMENT_ID"));
            notification.setReceiverDepartmentId(rs.getInt("RECEIVER_DEPARTMENT_ID"));
            notification.setFirstReaderId(rs.getInt("FIRST_READER_ID"));
            notification.setText(rs.getString("TEXT"));
            notification.setCreateDate(new Date(rs.getTimestamp("CREATE_DATE").getTime()));
            notification.setDeadline(rs.getDate("DEADLINE"));
            return notification;
        }
    }

    @Override
    public int save(Notification notification) {
        JdbcTemplate jt = getJdbcTemplate();

        Integer id = notification.getId();
        if (id == null) {
            id = generateId("seq_notification", Integer.class);
        }

        jt.update(
                "insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, " +
                        "FIRST_READER_ID, TEXT, CREATE_DATE, DEADLINE)" +
                        " values (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                notification.getReportPeriodId(),
                notification.getSenderDepartmentId(),
                notification.getReceiverDepartmentId(),
                notification.getFirstReaderId(),
                notification.getText(),
                notification.getCreateDate(),
                notification.getDeadline()
        );
        notification.setId(id);
        return id;
    }

    @Override
    public Notification get(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId) {
        try {
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        String query = "select * from notification where " +
			        "REPORT_PERIOD_ID = :rpid and " +
			        "SENDER_DEPARTMENT_ID = :sdid and " +
			        "RECEIVER_DEPARTMENT_ID " + (receiverDepartmentId == null ? "is null" : "= :rdid") + "";
	        params.addValue("rpid", reportPeriodId);
	        params.addValue("sdid", senderDepartmentId);
	        if (receiverDepartmentId != null) {
		        params.addValue("rdid", receiverDepartmentId);
	        }

	        return getNamedParameterJdbcTemplate().queryForObject(query, params, new NotificationMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Notification> listByDepartments(int senderDepartmentId, Integer receiverDepartmentId) {
        return getJdbcTemplate().query(
                "select * from notification where SENDER_DEPARTMENT_ID = ? and RECEIVER_DEPARTMENT_ID = ?",
                new Object[]{senderDepartmentId, receiverDepartmentId},
                new int[]{Types.NUMERIC, Types.NUMERIC},
                new NotificationMapper()
        );
    }

    @Override
    public void saveList(final List<Notification> notifications) {
        getJdbcTemplate().batchUpdate("insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, " +
                "FIRST_READER_ID, TEXT, CREATE_DATE, DEADLINE)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Notification elem = notifications.get(i);
                ps.setInt(1, generateId("seq_notification", Integer.class));
                ps.setInt(2, elem.getReportPeriodId());
                ps.setInt(3, elem.getSenderDepartmentId());
                if (elem.getReceiverDepartmentId() != null) {
                    ps.setInt(4, elem.getReceiverDepartmentId());
                } else {
                    ps.setNull(4, Types.NUMERIC);
                }
                if (elem.getFirstReaderId() != null) {
                    ps.setInt(5, elem.getFirstReaderId());
                } else {
                    ps.setNull(5, Types.NUMERIC);
                }
                ps.setString(6, elem.getText());
                ps.setTimestamp(7, new java.sql.Timestamp(elem.getCreateDate().getTime()));
                ps.setDate(8, new java.sql.Date(elem.getDeadline().getTime()));
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
    }

    @Override
    public void delete(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId) {
        getJdbcTemplate().update("delete from notification where REPORT_PERIOD_ID = ? and SENDER_DEPARTMENT_ID = ? and RECEIVER_DEPARTMENT_ID = ?",
                new Object[]{reportPeriodId, senderDepartmentId, receiverDepartmentId},
                new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
    }

    @Override
    public void deleteList(int reportPeriodId, List<DepartmentPair> departments) {
        StringBuilder sql = new StringBuilder("delete from notification where REPORT_PERIOD_ID = ? and (");
        for (int i = 0; i < departments.size(); i++) {
            DepartmentPair pair = departments.get(i);
            sql.append("(SENDER_DEPARTMENT_ID=").append(pair.getDepartmentId())
                    .append(" and RECEIVER_DEPARTMENT_ID ").append(pair.getParentDepartmentId() == null
		            ? " is null " : " = " + pair.getParentDepartmentId()).append(")");
            if (i < departments.size() - 1) {
                sql.append(" or ");
            }
        }
        sql.append(")");
        getJdbcTemplate().update(sql.toString(),
                new Object[]{reportPeriodId},
                new int[]{Types.NUMERIC});
    }

	@Override
	public List<Integer> listForDepartment(int departmentId) {
		try {
			String query = "select id from notification where RECEIVER_DEPARTMENT_ID = :rdid";
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("rdid", departmentId);
			return getNamedParameterJdbcTemplate().queryForList(query, params, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public Notification get(int id) {
		try {
			String query = "select * from notification where id = :id";
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", id);
			return getNamedParameterJdbcTemplate().queryForObject(query, params, new NotificationMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Integer> getByFilter(NotificationsFilterData filter) {
		try {
			StringBuilder query = new StringBuilder("select id from ( select rownum as rn, id from (select nt.id, " +
					"nt.RECEIVER_DEPARTMENT_ID, nt.SENDER_DEPARTMENT_ID, nt.create_date from notification nt where 1=1 ");
			MapSqlParameterSource params = new MapSqlParameterSource();
			if (filter.getReceiverDepartmentId() != null) {
				params.addValue("rdid", filter.getReceiverDepartmentId());
				query.append( " and nt.RECEIVER_DEPARTMENT_ID = :rdid");
			}
			if (filter.getSenderDepartmentId() != null) {
				params.addValue("sdid", filter.getSenderDepartmentId());
				query.append(" and nt.SENDER_DEPARTMENT_ID = :sdid");
			}
			query.append(" order by nt.create_date desc ");
			query.append("))");
			if ((filter.getStartIndex() != null) && (filter.getCountOfRecords() != null)) {
				params.addValue("start", filter.getStartIndex() + 1);
				params.addValue("end", filter.getStartIndex() + filter.getCountOfRecords());
				query.append(" where rn between :start and :end");
			}

			return getNamedParameterJdbcTemplate().queryForList(query.toString(), params, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public int getCountByFilter(NotificationsFilterData filter) {
		try {
			MapSqlParameterSource params = new MapSqlParameterSource();
			StringBuilder query = new StringBuilder("select count(*) from notification where 1=1");
			if (filter.getReceiverDepartmentId() != null) {
				params.addValue("rdid", filter.getReceiverDepartmentId());
				query.append( " and RECEIVER_DEPARTMENT_ID = :rdid");
			}
			if (filter.getSenderDepartmentId() != null) {
				params.addValue("sdid", filter.getSenderDepartmentId());
				query.append(" and SENDER_DEPARTMENT_ID = :sdid");
			}
			return getNamedParameterJdbcTemplate().queryForInt(query.toString(), params);
		} catch (EmptyResultDataAccessException e) {
			return 0;
		}
	}

    @Override
    public void deleteByReportPeriod(int reportPeriodId) {
        getJdbcTemplate().update("delete from notification where REPORT_PERIOD_ID = ?",
                new Object[]{reportPeriodId},
                new int[]{Types.NUMERIC});
    }
}
