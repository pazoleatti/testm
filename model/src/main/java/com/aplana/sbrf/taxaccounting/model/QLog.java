package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLog is a Querydsl query type for QLog
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLog extends com.querydsl.sql.RelationalPathBase<QLog> {

    private static final long serialVersionUID = 363152647;

    public static final QLog log = new QLog("LOG");

    public final DateTimePath<java.sql.Timestamp> creationDate = createDateTime("creationDate", java.sql.Timestamp.class);

    public final StringPath id = createString("id");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QLog> logPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSecUser> logUserFk = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QLogSystem> _logSystemLogFk = createInvForeignKey(id, "LOG_ID");

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationLogFk = createInvForeignKey(id, "LOG_ID");

    public final com.querydsl.sql.ForeignKey<QLogEntry> _logEntryLogFk = createInvForeignKey(id, "LOG_ID");

    public QLog(String variable) {
        super(QLog.class, forVariable(variable), "NDFL_UNSTABLE", "LOG");
        addMetadata();
    }

    public QLog(String variable, String schema, String table) {
        super(QLog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLog(Path<? extends QLog> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOG");
        addMetadata();
    }

    public QLog(PathMetadata metadata) {
        super(QLog.class, metadata, "NDFL_UNSTABLE", "LOG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationDate, ColumnMetadata.named("CREATION_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18));
    }

}

