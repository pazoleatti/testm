package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
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
            notification.setId(SqlUtils.getInteger(rs, "ID"));
            notification.setReportPeriodId(SqlUtils.getInteger(rs, "REPORT_PERIOD_ID"));
            notification.setSenderDepartmentId(SqlUtils.getInteger(rs, "SENDER_DEPARTMENT_ID"));
            notification.setReceiverDepartmentId(SqlUtils.getInteger(rs, "RECEIVER_DEPARTMENT_ID"));
            notification.setRead(rs.getBoolean("IS_READ"));
            notification.setText(rs.getString("TEXT"));
            notification.setCreateDate(new Date(rs.getTimestamp("CREATE_DATE").getTime()));
            notification.setDeadline(rs.getDate("DEADLINE"));
            notification.setUserId(rs.getInt("USER_ID"));
            notification.setRoleId(rs.getInt("ROLE_ID"));
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
                        "IS_READ, TEXT, CREATE_DATE, DEADLINE)" +
                        " values (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                notification.getReportPeriodId(),
                notification.getSenderDepartmentId(),
                notification.getReceiverDepartmentId(),
                notification.isRead() ? 1 : 0,
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
    public void saveList(final List<Notification> notifications) {
        getJdbcTemplate().batchUpdate("insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, " +
                "IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Notification elem = notifications.get(i);
                ps.setInt(1, generateId("seq_notification", Integer.class));
                //Отчетный период
                if (elem.getReportPeriodId() != null) {
                    ps.setInt(2, elem.getReportPeriodId());
                } else {
                    ps.setNull(2, Types.NUMERIC);
                }
                //Подразделение-отправитель
                if (elem.getSenderDepartmentId() != null) {
                    ps.setInt(3, elem.getSenderDepartmentId());
                } else {
                    ps.setNull(3, Types.NUMERIC);
                }
                //Подразделение-получатель
                if (elem.getReceiverDepartmentId() != null) {
                    ps.setInt(4, elem.getReceiverDepartmentId());
                } else {
                    ps.setNull(4, Types.NUMERIC);
                }
                //Признак прочтения
                ps.setInt(5, elem.isRead() ? 1 : 0);
                //Текст
                ps.setString(6, elem.getText());
                //Дата создания
                ps.setTimestamp(7, new java.sql.Timestamp(elem.getCreateDate().getTime()));
                //Срок сдачи отчетности
                if (elem.getDeadline() != null) {
                    ps.setDate(8, new java.sql.Date(elem.getDeadline().getTime()));
                } else {
                    ps.setNull(8, Types.DATE);
                }
                //Пользователь-получатель  сообщения
                if (elem.getUserId() != null) {
                    ps.setInt(9, elem.getUserId());
                } else {
                    ps.setNull(9, Types.NUMERIC);
                }
                //Роль пользователей-получателей сообщения
                if (elem.getRoleId() != null) {
                    ps.setInt(10, elem.getRoleId());
                } else {
                    ps.setNull(10, Types.NUMERIC);
                }
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
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

    private static final String GET_BY_FILTER = "select * from (\n" +
            "  select ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID, \n" +
            " row_number() %s as rn \n" +
            "  from notification \n" +
            "where (\n" +
            "(:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "(:userId is not null and USER_ID = :userId)%s%s \n" +
            ") and (:read is null or IS_READ = :read) \n" +
            ")";

	@Override
	public List<Notification> getByFilter(NotificationsFilterData filter) {
		try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            StringBuilder sort = new StringBuilder();
            switch (filter.getSortColumn()){
                case DATE:
                    sort.append("CREATE_DATE ");
                    break;
                case TEXT:
                    sort.append("TEXT ");
                    break;
                default:
                    sort.append("CREATE_DATE ");
                    break;
            }
            sort.append(filter.isAsc() ? "ASC " : "DESC ");

            StringBuilder sql = new StringBuilder(String.format(GET_BY_FILTER,
                    isSupportOver() ? "over (order by " + sort.toString() + ")" : "over()",
                    filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty() ? ""
                            : " or \n" + SqlUtils.transformToSqlInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                    filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty() ? ""
                            : " or \n" + SqlUtils.transformToSqlInStatement("ROLE_ID", filter.getUserRoleIds())));

            //Фильтры по типу оповещения
            params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
            params.addValue("userId", filter.getUserId());

            //Дополнительные фильтры
            params.addValue("read", filter.isRead() == null ? null : filter.isRead());

            //Пэйджинг
			if ((filter.getStartIndex() != null) && (filter.getCountOfRecords() != null)) {
				params.addValue("start", filter.getStartIndex() + 1);
				params.addValue("end", filter.getStartIndex() + filter.getCountOfRecords());
                sql.append(" where rn between :start and :end");
			}

			return getNamedParameterJdbcTemplate().query(sql.toString(), params, new NotificationMapper());
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

    private static final String GET_COUNT_BY_FILTER = "select count(id) from notification \n" +
            "where (\n" +
            "(:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "(:userId is not null and USER_ID = :userId)%s%s \n" +
            ") and (:read is null or IS_READ = :read)";

	@Override
	public int getCountByFilter(NotificationsFilterData filter) {
		try {
            String sql = String.format(GET_COUNT_BY_FILTER,
                    filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty() ? ""
                            : " or \n" + SqlUtils.transformToSqlInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                    filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty() ? ""
                            : " or \n" + SqlUtils.transformToSqlInStatement("ROLE_ID", filter.getUserRoleIds()));
			MapSqlParameterSource params = new MapSqlParameterSource();
            //Фильтры по типу оповещения
            params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
            params.addValue("userId", filter.getUserId());

            //Дополнительные фильтры
            params.addValue("read", filter.isRead() == null ? null : filter.isRead());
			return getNamedParameterJdbcTemplate().queryForInt(sql, params);
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

    private static final String UPDATE_USER_NOTIFICATIONS_STATUS = "update notification set IS_READ = 1 \n" +
            "where (\n" +
            "(:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "(:userId is not null and USER_ID = :userId)%s%s \n" +
            ") and IS_READ = 0";

    @Override
    public void updateUserNotificationsStatus(NotificationsFilterData filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = String.format(UPDATE_USER_NOTIFICATIONS_STATUS,
                filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty() ? ""
                        : " or \n" + SqlUtils.transformToSqlInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty() ? ""
                        : " or \n" + SqlUtils.transformToSqlInStatement("ROLE_ID", filter.getUserRoleIds()));

        //Фильтры по типу оповещения
        params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
        params.addValue("userId", filter.getUserId());
        getNamedParameterJdbcTemplate().update(sql, params);
    }
}
