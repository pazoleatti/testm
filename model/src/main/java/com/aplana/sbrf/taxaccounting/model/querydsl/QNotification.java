package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNotification is a Querydsl query type for QNotification
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNotification extends com.querydsl.sql.RelationalPathBase<QNotification> {

    private static final long serialVersionUID = -459773833;

    public static final QNotification notification = new QNotification("NOTIFICATION");

    public final DateTimePath<org.joda.time.LocalDateTime> createDate = createDateTime("createDate", org.joda.time.LocalDateTime.class);

    public final DateTimePath<org.joda.time.LocalDateTime> deadline = createDateTime("deadline", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final StringPath logId = createString("logId");

    public final NumberPath<Integer> receiverDepartmentId = createNumber("receiverDepartmentId", Integer.class);

    public final StringPath reportId = createString("reportId");

    public final NumberPath<Integer> reportPeriodId = createNumber("reportPeriodId", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final NumberPath<Integer> senderDepartmentId = createNumber("senderDepartmentId", Integer.class);

    public final StringPath text = createString("text");

    public final EnumPath<com.aplana.sbrf.taxaccounting.model.NotificationType> type = createEnum("type", com.aplana.sbrf.taxaccounting.model.NotificationType.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QNotification> notificationPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> notificationFkSender = createForeignKey(senderDepartmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> notificationFkReportId = createForeignKey(reportId, "ID");

    public final com.querydsl.sql.ForeignKey<QSecUser> notificationFkNotifyUser = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QReportPeriod> notificationFkReportPeriod = createForeignKey(reportPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> notificationFkReceiver = createForeignKey(receiverDepartmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QLog> notificationLogFk = createForeignKey(logId, "ID");

    public final com.querydsl.sql.ForeignKey<QSecRole> notificationFkNotifyRole = createForeignKey(roleId, "ID");

    public QNotification(String variable) {
        super(QNotification.class, forVariable(variable), "NDFL_UNSTABLE", "NOTIFICATION");
        addMetadata();
    }

    public QNotification(String variable, String schema, String table) {
        super(QNotification.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNotification(String variable, String schema) {
        super(QNotification.class, forVariable(variable), schema, "NOTIFICATION");
        addMetadata();
    }

    public QNotification(Path<? extends QNotification> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NOTIFICATION");
        addMetadata();
    }

    public QNotification(PathMetadata metadata) {
        super(QNotification.class, metadata, "NDFL_UNSTABLE", "NOTIFICATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createDate, ColumnMetadata.named("CREATE_DATE").withIndex(6).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(deadline, ColumnMetadata.named("DEADLINE").withIndex(7).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(isRead, ColumnMetadata.named("IS_READ").withIndex(10).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(logId, ColumnMetadata.named("LOG_ID").withIndex(13).ofType(Types.VARCHAR).withSize(36));
        addMetadata(receiverDepartmentId, ColumnMetadata.named("RECEIVER_DEPARTMENT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(reportId, ColumnMetadata.named("REPORT_ID").withIndex(12).ofType(Types.VARCHAR).withSize(36));
        addMetadata(reportPeriodId, ColumnMetadata.named("REPORT_PERIOD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(roleId, ColumnMetadata.named("ROLE_ID").withIndex(9).ofType(Types.DECIMAL).withSize(9));
        addMetadata(senderDepartmentId, ColumnMetadata.named("SENDER_DEPARTMENT_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9));
        addMetadata(text, ColumnMetadata.named("TEXT").withIndex(5).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(11).ofType(Types.DECIMAL).withSize(2).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(8).ofType(Types.DECIMAL).withSize(9));
    }

}

