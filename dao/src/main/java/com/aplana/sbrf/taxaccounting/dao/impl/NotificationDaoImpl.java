package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Repository
public class NotificationDaoImpl extends AbstractDao implements NotificationDao {

    @Autowired
    private DBUtils dbUtils;

    private class NotificationMapper implements RowMapper<Notification> {
        @Override
        public Notification mapRow(ResultSet rs, int index) throws SQLException {
            Notification notification = new Notification();
            notification.setId(SqlUtils.getLong(rs, "ID"));
            notification.setReportPeriodId(SqlUtils.getInteger(rs, "REPORT_PERIOD_ID"));
            notification.setSenderDepartmentId(SqlUtils.getInteger(rs, "SENDER_DEPARTMENT_ID"));
            notification.setReceiverDepartmentId(SqlUtils.getInteger(rs, "RECEIVER_DEPARTMENT_ID"));
            notification.setRead(rs.getBoolean("IS_READ"));
            notification.setText(rs.getString("TEXT"));
            notification.setLogId(rs.getString("LOG_ID"));
            notification.setCreateDate(new Date(rs.getTimestamp("CREATE_DATE").getTime()));
            notification.setDeadline(rs.getDate("DEADLINE"));
            notification.setUserId(SqlUtils.getInteger(rs, "USER_ID"));
            notification.setRoleId(SqlUtils.getInteger(rs, "ROLE_ID"));
            notification.setReportId(rs.getString("REPORT_ID"));
            notification.setNotificationType(NotificationType.fromId(rs.getInt("TYPE")));
            return notification;
        }
    }

    @Override
    public Notification fetchOne(long id) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            return getNamedParameterJdbcTemplate().queryForObject(
                    "select * from notification where id = :id", params, new NotificationMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Notification fetchOne(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("rpid", reportPeriodId);
            params.addValue("sdid", senderDepartmentId);
            if (receiverDepartmentId != null) {
                params.addValue("rdid", receiverDepartmentId);
            }

            return getNamedParameterJdbcTemplate().queryForObject(
                    "select * from notification where " +
                            "REPORT_PERIOD_ID = :rpid and " +
                            "SENDER_DEPARTMENT_ID " + (senderDepartmentId == null ? "is null" : "= :sdid") + " and " +
                            "RECEIVER_DEPARTMENT_ID " + (receiverDepartmentId == null ? "is null" : "= :rdid") + "",
                    params, new NotificationMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void create(final List<Notification> notifications) {
        final List<Long> ids = dbUtils.getNextIds("seq_notification", notifications.size());

        getJdbcTemplate().batchUpdate("insert into notification (ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, " +
                "IS_READ, TEXT, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID, LOG_ID, TYPE, REPORT_ID)" +
                " values (?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Notification elem = notifications.get(i);
                elem.setId(ids.get(i));
                ps.setLong(1, ids.get(i));
                ps.setObject(2, elem.getReportPeriodId(), Types.NUMERIC);
                ps.setObject(3, elem.getSenderDepartmentId(), Types.NUMERIC);
                ps.setObject(4, elem.getReceiverDepartmentId(), Types.NUMERIC);
                ps.setBoolean(5, elem.isRead());
                ps.setString(6, elem.getText());
                ps.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
                ps.setDate(8, elem.getDeadline() != null ? new java.sql.Date(elem.getDeadline().getTime()) : null);
                ps.setObject(9, elem.getUserId(), Types.NUMERIC);
                ps.setObject(10, elem.getRoleId(), Types.NUMERIC);
                ps.setString(11, elem.getLogId());
                ps.setInt(12, elem.getNotificationType().getId());
                ps.setString(13, elem.getReportId());
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
    }

    @Override
    public void delete(int reportPeriodId, List<DepartmentPair> departments) {
        StringBuilder sql = new StringBuilder("delete from notification where REPORT_PERIOD_ID = ? and (");
        for (int i = 0; i < departments.size(); i++) {
            DepartmentPair pair = departments.get(i);
            sql.append("(SENDER_DEPARTMENT_ID ")
                    .append(pair.getDepartmentId() == null ? "is null " : " = " + pair.getDepartmentId())
                    .append(" and RECEIVER_DEPARTMENT_ID ")
                    .append(pair.getParentDepartmentId() == null ? " is null " : " = " + pair.getParentDepartmentId()).append(")");
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
    public void deleteByReportPeriod(int reportPeriodId) {
        getJdbcTemplate().update("delete from notification where REPORT_PERIOD_ID = ?",
                reportPeriodId);
    }

    @Override
    public void deleteAll(List<Long> notificationIds) {
        getJdbcTemplate().update("delete from notification where " + SqlUtils.transformToSqlInStatement("id", notificationIds));
    }

    @Override
    public List<Notification> fetchAllByFilter(NotificationsFilterData filter) {
        PagingParams pagingParams = null;
        // Только для GWT, неGWT использует pagingParams, а не filter
        if (filter.getStartIndex() != null && filter.getCountOfRecords() != null) {
            pagingParams = new PagingParams(filter.getStartIndex(), filter.getCountOfRecords());
            switch (filter.getSortColumn()) {
                case DATE:
                    pagingParams.setProperty("CREATE_DATE");
                    break;
                case TEXT:
                    pagingParams.setProperty("TEXT");
                    break;
                default:
                    pagingParams.setProperty("CREATE_DATE");
                    break;
            }
            pagingParams.setDirection(filter.isAsc() ? "ASC" : "DESC");
        }

        return fetchAllByFilterAndPaging(filter, pagingParams);
    }


    private static final String GET_BY_FILTER = "" +
            "select * from (\n" +
            "   select ID, REPORT_PERIOD_ID, SENDER_DEPARTMENT_ID, RECEIVER_DEPARTMENT_ID, IS_READ, TEXT, LOG_ID, CREATE_DATE, DEADLINE, USER_ID, ROLE_ID, REPORT_ID, TYPE, \n" +
            "           row_number() %s as rn \n" +
            "   from notification \n" +
            "   where (\n" +
            "       (:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "       (:userId is not null and USER_ID = :userId)%s%s \n" +
            "   ) and (:read is null or IS_READ = :read) \n" +
            "   %s" +
            ")";

    @Override
    public List<Notification> fetchAllByFilterAndPaging(NotificationsFilterData filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String orderClause = isSupportOver() ? "over (order by " + pagingParams.getProperty() + " " + pagingParams.getDirection() + ")" : "over()";

        String conditions = "";
        if (isNotEmpty(filter.getText())) {
            conditions = conditions + " and lower(TEXT) like lower(:text) ";
            params.addValue("text", "%" + filter.getText() + "%");
        }
        if (filter.getTimeFrom() != null) {
            conditions = conditions + " and (CREATE_DATE >= :timeFrom) ";
            params.addValue("timeFrom", filter.getTimeFrom());
        }
        if (filter.getTimeTo() != null) {
            conditions += " and (CREATE_DATE <= :timeTo) ";
            params.addValue("timeTo", filter.getTimeTo());
        }

        StringBuilder sql = new StringBuilder(String.format(GET_BY_FILTER,
                orderClause,
                orInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                orInStatement("ROLE_ID", filter.getUserRoleIds()),
                conditions
        ));

        params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
        params.addValue("userId", filter.getUserId());
        params.addValue("read", filter.isRead());

        if (pagingParams != null) {
            params.addValue("start", pagingParams.getStartIndex() + 1);
            params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
            sql.append(" where rn between :start and :end");
        }

        return getNamedParameterJdbcTemplate().query(sql.toString(), params, new NotificationMapper());
    }

    private static final String GET_COUNT_BY_FILTER = "select count(id) from notification \n" +
            "where (\n" +
            "   (:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "   (:userId is not null and USER_ID = :userId)%s%s \n" +
            ") and (:read is null or IS_READ = :read) %s";

    @Override
    public int fetchCountByFilter(NotificationsFilterData filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String conditions = "";
        if (isNotEmpty(filter.getText())) {
            conditions = conditions + " and lower(TEXT) like lower(:text) ";
            params.addValue("text", "%" + filter.getText() + "%");
        }
        if (filter.getTimeFrom() != null) {
            conditions = conditions + " and (CREATE_DATE >= :timeFrom) ";
            params.addValue("timeFrom", filter.getTimeFrom());
        }
        if (filter.getTimeTo() != null) {
            conditions += " and (CREATE_DATE <= :timeTo) ";
            params.addValue("timeTo", filter.getTimeTo());
        }

        String sql = String.format(GET_COUNT_BY_FILTER,
                orInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                orInStatement("ROLE_ID", filter.getUserRoleIds()),
                conditions);

        params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
        params.addValue("userId", filter.getUserId());
        params.addValue("read", filter.isRead());

        return getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class);
    }

    private static final String UPDATE_USER_NOTIFICATIONS_STATUS = "update notification set IS_READ = 1 \n" +
            "where (\n" +
            "(:senderDepartmentId is not null and SENDER_DEPARTMENT_ID = :senderDepartmentId) or \n" +
            "(:userId is not null and USER_ID = :userId)%s%s \n" +
            ") and IS_READ = 0";

    @Override
    public void updateReadTrueByFilter(NotificationsFilterData filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = String.format(UPDATE_USER_NOTIFICATIONS_STATUS,
                orInStatement("RECEIVER_DEPARTMENT_ID", filter.getReceiverDepartmentIds()),
                orInStatement("ROLE_ID", filter.getUserRoleIds()));

        //Фильтры по типу оповещения
        params.addValue("senderDepartmentId", filter.getSenderDepartmentId());
        params.addValue("userId", filter.getUserId());
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public Date fetchLastNotificationDate() {
        Date lastDate = getJdbcTemplate().queryForObject("select max(CREATE_DATE) from NOTIFICATION", Date.class);
        if (lastDate != null) {
            return new Date(lastDate.getTime());
        }
        return null;
    }

    @Override
    public boolean isExistsNotificationBlobForUser(int userId, String blobId) {
        String sql = "select count(*) from NOTIFICATION where user_id = :userId and report_id = :blobId";
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        params.addValue("blobId", blobId);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, Integer.class) > 0;
    }

    private String orInStatement(String prefix, List<Integer> numbers) {
        return numbers == null || numbers.isEmpty() ?
                "" : " or \n" + SqlUtils.transformToSqlInStatement(prefix, numbers);
    }
}