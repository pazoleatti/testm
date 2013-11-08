package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
            notification.setCreateDate(rs.getDate("CREATE_DATE"));
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
            return getJdbcTemplate().queryForObject(
                    "select * from notification where REPORT_PERIOD_ID = ? and SENDER_DEPARTMENT_ID = ? and RECEIVER_DEPARTMENT_ID = ?",
                    new Object[]{reportPeriodId, senderDepartmentId, receiverDepartmentId},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new NotificationMapper()
            );
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
                ps.setInt(4, elem.getReceiverDepartmentId());
                ps.setInt(5, elem.getFirstReaderId());
                ps.setString(6, elem.getText());
                ps.setDate(7, new java.sql.Date(elem.getCreateDate().getTime()));
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
                    .append(" and RECEIVER_DEPARTMENT_ID=").append(pair.getParentDepartmentId()).append(")");
            if (i < departments.size() - 1) {
                sql.append(" or ");
            }
        }
        sql.append(")");
        System.out.println("sql: "+sql);
        getJdbcTemplate().update(sql.toString(),
                new Object[]{reportPeriodId},
                new int[]{Types.NUMERIC});
    }
}
