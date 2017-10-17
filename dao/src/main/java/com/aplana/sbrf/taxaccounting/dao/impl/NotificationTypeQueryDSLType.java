package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.querydsl.sql.types.AbstractType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationTypeQueryDSLType extends AbstractType<NotificationType> {

    public NotificationTypeQueryDSLType(int type) {
        super(type);
    }

    @Override
    public Class<NotificationType> getReturnedClass() {
        return NotificationType.class;
    }

    @Override
    public NotificationType getValue(ResultSet resultSet, int i) throws SQLException {
        return NotificationType.fromId(resultSet.getInt(i));
    }

    @Override
    public void setValue(PreparedStatement preparedStatement, int i, NotificationType notificationType) throws SQLException {
        preparedStatement.setInt(i, notificationType.getId());
    }
}
