package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTaskContext is a Querydsl query type for QTaskContext
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTaskContext extends com.querydsl.sql.RelationalPathBase<QTaskContext> {

    private static final long serialVersionUID = 1708357421;

    public static final QTaskContext taskContext = new QTaskContext("TASK_CONTEXT");

    public final NumberPath<Integer> customParamsExist = createNumber("customParamsExist", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> modificationDate = createDateTime("modificationDate", org.joda.time.LocalDateTime.class);

    public final SimplePath<java.io.InputStream> serializedParams = createSimple("serializedParams", java.io.InputStream.class);

    public final NumberPath<Long> taskId = createNumber("taskId", Long.class);

    public final StringPath taskName = createString("taskName");

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final StringPath userTaskJndi = createString("userTaskJndi");

    public final com.querydsl.sql.PrimaryKey<QTaskContext> taskContextPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSecUser> taskContextFkUserId = createForeignKey(userId, "ID");

    public QTaskContext(String variable) {
        super(QTaskContext.class, forVariable(variable), "NDFL_UNSTABLE", "TASK_CONTEXT");
        addMetadata();
    }

    public QTaskContext(String variable, String schema, String table) {
        super(QTaskContext.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTaskContext(Path<? extends QTaskContext> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TASK_CONTEXT");
        addMetadata();
    }

    public QTaskContext(PathMetadata metadata) {
        super(QTaskContext.class, metadata, "NDFL_UNSTABLE", "TASK_CONTEXT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(customParamsExist, ColumnMetadata.named("CUSTOM_PARAMS_EXIST").withIndex(6).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(modificationDate, ColumnMetadata.named("MODIFICATION_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(serializedParams, ColumnMetadata.named("SERIALIZED_PARAMS").withIndex(7).ofType(Types.BLOB).withSize(4000));
        addMetadata(taskId, ColumnMetadata.named("TASK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(taskName, ColumnMetadata.named("TASK_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(8).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(userTaskJndi, ColumnMetadata.named("USER_TASK_JNDI").withIndex(5).ofType(Types.VARCHAR).withSize(500).notNull());
    }

}

