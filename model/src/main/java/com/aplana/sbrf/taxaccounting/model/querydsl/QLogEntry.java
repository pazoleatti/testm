package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogEntry is a Querydsl query type for QLogEntry
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogEntry extends com.querydsl.sql.RelationalPathBase<QLogEntry> {

    private static final long serialVersionUID = 1867080986;

    public static final QLogEntry logEntry = new QLogEntry("LOG_ENTRY");

    public final DateTimePath<org.joda.time.LocalDateTime> creationDate = createDateTime("creationDate", org.joda.time.LocalDateTime.class);

    public final StringPath logId = createString("logId");

    public final EnumPath<com.aplana.sbrf.taxaccounting.model.log.LogLevel> logLevel = createEnum("logLevel", com.aplana.sbrf.taxaccounting.model.log.LogLevel.class);

    public final StringPath message = createString("message");

    public final StringPath object = createString("object");

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<QLogEntry> logEntryPk = createPrimaryKey(logId, ord);

    public final com.querydsl.sql.ForeignKey<QLog> logEntryLogFk = createForeignKey(logId, "ID");

    public QLogEntry(String variable) {
        super(QLogEntry.class, forVariable(variable), "NDFL_UNSTABLE", "LOG_ENTRY");
        addMetadata();
    }

    public QLogEntry(String variable, String schema, String table) {
        super(QLogEntry.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogEntry(String variable, String schema) {
        super(QLogEntry.class, forVariable(variable), schema, "LOG_ENTRY");
        addMetadata();
    }

    public QLogEntry(Path<? extends QLogEntry> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOG_ENTRY");
        addMetadata();
    }

    public QLogEntry(PathMetadata metadata) {
        super(QLogEntry.class, metadata, "NDFL_UNSTABLE", "LOG_ENTRY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationDate, ColumnMetadata.named("CREATION_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(logId, ColumnMetadata.named("LOG_ID").withIndex(1).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(logLevel, ColumnMetadata.named("LOG_LEVEL").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(message, ColumnMetadata.named("MESSAGE").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(object, ColumnMetadata.named("OBJECT").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(255));
    }

}

