package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.util.QueryDSLOrderingUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QNotification.notification;
import static com.querydsl.core.types.Projections.bean;
import static com.querydsl.sql.oracle.OracleGrammar.sysdate;

@Repository
public class NotificationDaoImpl extends AbstractDao implements NotificationDao {

    final private SQLQueryFactory sqlQueryFactory;

    public NotificationDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<Notification> notificationBean = bean(Notification.class, notification.all());

    @Override
    public long save(Notification notificationForSave) {
        Long id = notificationForSave.getId();
        if (id == null) {
            id = generateId("seq_notification", Long.class);
        }

        sqlQueryFactory.insert(notification)
                .columns(notification.id, notification.reportPeriodId, notification.senderDepartmentId
                        , notification.receiverDepartmentId, notification.isRead, notification.text
                        , notification.createDate, notification.deadline, notification.logId
                        , notification.reportId, notification.type)
                .values(id, notificationForSave.getReportPeriodId(), notificationForSave.getSenderDepartmentId()
                        , notificationForSave.getReceiverDepartmentId(), notificationForSave.isRead() ? 1 : 0, notificationForSave.getText()
                        , notificationForSave.getCreateDate(), notificationForSave.getDeadline(), notificationForSave.getLogId()
                        , notificationForSave.getReportId(), notificationForSave.getNotificationType().getId())
                .execute();
        notificationForSave.setId(id);
        return id;
    }

    @Override
    public Notification get(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(notification.reportPeriodId.eq(reportPeriodId));
        if (senderDepartmentId == null) {
            where.and(notification.senderDepartmentId.isNull());
        } else {
            where.and(notification.senderDepartmentId.eq(senderDepartmentId));
        }
        if (receiverDepartmentId == null) {
            where.and(notification.receiverDepartmentId.isNull());
        } else {
            where.and(notification.receiverDepartmentId.eq(receiverDepartmentId));
        }

        List<Notification> notifications = sqlQueryFactory.from(notification)
                .where(where)
                .transform(GroupBy.groupBy(notification.id).list(notificationBean));
        return notifications.isEmpty() ? null : notifications.get(0);
    }

    @Override
    public void saveList(final List<Notification> notifications) {
        Long id;
        for (Notification tmpNotification : notifications) {
            id = tmpNotification.getId();
            if (id == null) {
                id = generateId("seq_notification", Long.class);
            }
            sqlQueryFactory.insert(notification)
                    .columns(notification.id, notification.reportPeriodId, notification.senderDepartmentId
                            , notification.receiverDepartmentId, notification.isRead, notification.text
                            , notification.createDate, notification.deadline, notification.userId
                            , notification.roleId, notification.logId, notification.reportId, notification.type)
                    .values(id, tmpNotification.getReportPeriodId(), tmpNotification.getSenderDepartmentId()
                            , tmpNotification.getReceiverDepartmentId(), tmpNotification.isRead() ? 1 : 0, tmpNotification.getText()
                            , tmpNotification.getCreateDate(), tmpNotification.getDeadline(), tmpNotification.getUserId()
                            , tmpNotification.getRoleId(), tmpNotification.getLogId(), tmpNotification.getReportId(), tmpNotification.getNotificationType().getId())
                    .execute();
        }
    }

    @Override
    public void deleteList(int reportPeriodId, List<DepartmentPair> departments) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(notification.reportPeriodId.eq(reportPeriodId));
        BooleanBuilder innerWhere = new BooleanBuilder();
        for (DepartmentPair pair : departments) {
            if (pair.getDepartmentId() == null) {
                innerWhere.or(notification.senderDepartmentId.isNull());
            } else {
                innerWhere.or(notification.senderDepartmentId.eq(pair.getDepartmentId()));
            }
            if (pair.getParentDepartmentId() == null) {
                innerWhere.and(notification.receiverDepartmentId.isNull());
            } else {
                innerWhere.and(notification.receiverDepartmentId.eq(pair.getParentDepartmentId()));
            }
        }

        where.and(innerWhere);

        sqlQueryFactory.delete(notification)
                .where(where)
                .execute();
    }

    @Override
    public Notification get(long id) {
        List<Notification> notifications = sqlQueryFactory.from(notification)
                .where(notification.id.eq(id))
                .transform(GroupBy.groupBy(notification.id).list(notificationBean));

        return notifications.isEmpty() ? null : notifications.get(0);
    }

    @Override
    public List<Notification> getByFilter(NotificationsFilterData filter) {
        StringPath rn = Expressions.stringPath("rn");
        StringPath notificationTable = Expressions.stringPath("notification");
        OrderSpecifier order;
        switch (filter.getSortColumn()) {
            case DATE:
                order = filter.isAsc() ? notification.createDate.asc() : notification.createDate.desc();
                break;
            case TEXT:
                order = filter.isAsc() ? notification.text.asc() : notification.text.desc();
                break;
            default:
                order = filter.isAsc() ? notification.createDate.asc() : notification.createDate.desc();
                break;
        }

        BooleanBuilder where = new BooleanBuilder();
        if (filter.getSenderDepartmentId() != null) {
            where.and(notification.senderDepartmentId.eq(filter.getSenderDepartmentId()));
        }
        if (filter.getUserId() != null) {
            where.and(notification.userId.eq(filter.getUserId()));
        }
        if (!(filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty())) {
            where.or(notification.receiverDepartmentId.in(filter.getReceiverDepartmentIds()));
        }
        if (!(filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty())) {
            where.or(notification.roleId.in(filter.getUserRoleIds()));
        }

        if (filter.isRead() != null) {
            where.and(notification.isRead.eq(filter.isRead() ? (byte) 1 : (byte) 0));
        }

        BooleanBuilder whereOut = new BooleanBuilder();
        if ((filter.getStartIndex() != null) && (filter.getCountOfRecords() != null)) {
            whereOut.and(rn.between(filter.getStartIndex().toString(), String.valueOf(filter.getStartIndex() + filter.getCountOfRecords())));
        }

        SimpleExpression subQuery = sqlQueryFactory.select(notification.id, notification.reportPeriodId, notification.senderDepartmentId, notification.receiverDepartmentId, notification.isRead
                , notification.text, notification.logId, notification.createDate, notification.deadline, notification.userId, notification.roleId, notification.reportId, notification.type
                , isSupportOver() ? SQLExpressions.rowNumber().over().orderBy(order).as("rn") : SQLExpressions.rowNumber().over().as("rn"))
                .from(notification)
                .where(where).as(notificationTable);

        return sqlQueryFactory.select(SQLExpressions.all).from(subQuery)
                .where(whereOut)
                .transform(GroupBy.groupBy(notification.id).list(notificationBean));
    }

    @Override
    public List<Notification> getByFilterWithPaging(NotificationsFilterData filter, PagingParams pagingParams) {
        StringPath rn = Expressions.stringPath("rn");
        StringPath notificationTable = Expressions.stringPath("notification");

        OrderSpecifier orderForRowNumber;
        switch (filter.getSortColumn()) {
            case DATE:
                orderForRowNumber = filter.isAsc() ? notification.createDate.asc() : notification.createDate.desc();
                break;
            case TEXT:
                orderForRowNumber = filter.isAsc() ? notification.text.asc() : notification.text.desc();
                break;
            default:
                orderForRowNumber = filter.isAsc() ? notification.createDate.asc() : notification.createDate.desc();
                break;
        }

        BooleanBuilder where = new BooleanBuilder();
        if (filter.getSenderDepartmentId() != null) {
            where.and(notification.senderDepartmentId.eq(filter.getSenderDepartmentId()));
        }
        if (filter.getUserId() != null) {
            where.and(notification.userId.eq(filter.getUserId()));
        }
        if (!(filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty())) {
            where.or(notification.receiverDepartmentId.in(filter.getReceiverDepartmentIds()));
        }
        if (!(filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty())) {
            where.or(notification.roleId.in(filter.getUserRoleIds()));
        }

        if (filter.isRead() != null) {
            where.and(notification.isRead.eq(filter.isRead() ? (byte) 1 : (byte) 0));
        }

        BooleanBuilder whereOut = new BooleanBuilder();
        if ((filter.getStartIndex() != null) && (filter.getCountOfRecords() != null)) {
            whereOut.and(rn.between(filter.getStartIndex().toString(), String.valueOf(filter.getStartIndex() + filter.getCountOfRecords())));
        }

        //Оперделяем способ сортировки
        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier orderForSubQuery = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                notificationBean, orderingProperty, ascDescOrder, notification.createDate.desc());

        SimpleExpression subQuery = sqlQueryFactory.select(notification.id, notification.reportPeriodId, notification.senderDepartmentId, notification.receiverDepartmentId, notification.isRead
                , notification.text, notification.logId, notification.createDate, notification.deadline, notification.userId, notification.roleId, notification.reportId, notification.type
                , isSupportOver() ? SQLExpressions.rowNumber().over().orderBy(orderForRowNumber).as("rn") : SQLExpressions.rowNumber().over().as("rn"))
                .from(notification)
                .orderBy(orderForSubQuery)
                .where(where).as(notificationTable);

        return sqlQueryFactory.select(SQLExpressions.all).from(subQuery)
                .where(whereOut)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(notification.id).list(notificationBean));
    }

    @Override
    public int getCountByFilter(NotificationsFilterData filter) {
        BooleanBuilder where = new BooleanBuilder();
        if (filter.getSenderDepartmentId() != null) {
            where.and(notification.senderDepartmentId.eq(filter.getSenderDepartmentId()));
        }
        if (filter.getUserId() != null) {
            where.or(notification.userId.eq(filter.getUserId()));
        }
        if (!(filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty())) {
            where.or(notification.receiverDepartmentId.in(filter.getReceiverDepartmentIds()));
        }
        if (!(filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty())) {
            where.or(notification.roleId.in(filter.getUserRoleIds()));
        }
        if (filter.isRead() != null) {
            where.and(notification.isRead.eq(filter.isRead() ? (byte) 1 : (byte) 0));
        }

        return (int) sqlQueryFactory.from(notification)
                .where(where)
                .fetchCount();
    }

    @Override
    public void deleteByReportPeriod(int reportPeriodId) {
        sqlQueryFactory.delete(notification).where(notification.reportPeriodId.eq(reportPeriodId)).execute();
    }

    @Override
    public void updateUserNotificationsStatus(NotificationsFilterData filter) {
        BooleanBuilder where = new BooleanBuilder();
        if (filter.getSenderDepartmentId() != null) {
            where.and(notification.senderDepartmentId.eq(filter.getSenderDepartmentId()));
        }
        if (filter.getUserId() != null) {
            where.or(notification.userId.eq(filter.getUserId()));
        }
        if (!(filter.getReceiverDepartmentIds() == null || filter.getReceiverDepartmentIds().isEmpty())) {
            where.or(notification.receiverDepartmentId.in(filter.getReceiverDepartmentIds()));
        }
        if (!(filter.getUserRoleIds() == null || filter.getUserRoleIds().isEmpty())) {
            where.or(notification.roleId.in(filter.getUserRoleIds()));
        }
        where.and(notification.isRead.eq((byte) 0));
        sqlQueryFactory.update(notification)
                .where(where)
                .set(notification.isRead, (byte) 1)
                .execute();
    }

    @Override
    public void deleteAll(List<Long> notificationIds) {
        sqlQueryFactory.delete(notification).where(notification.id.in(notificationIds)).execute();
    }

    @Override
    public Date getLastNotificationDate() {
        LocalDateTime lastDate = sqlQueryFactory.select(notification.createDate.max()).from(notification).fetchFirst();
        if (lastDate != null) {
            return lastDate.toDate();
        }
        return null;
    }
}
