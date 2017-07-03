package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAsyncTaskType is a Querydsl query type for QAsyncTaskType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAsyncTaskType extends com.querydsl.sql.RelationalPathBase<QAsyncTaskType> {

    private static final long serialVersionUID = 1383834366;

    public static final QAsyncTaskType asyncTaskType = new QAsyncTaskType("ASYNC_TASK_TYPE");

    public final NumberPath<Byte> devMode = createNumber("devMode", Byte.class);

    public final StringPath handlerJndi = createString("handlerJndi");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath limitKind = createString("limitKind");

    public final StringPath name = createString("name");

    public final NumberPath<Long> shortQueueLimit = createNumber("shortQueueLimit", Long.class);

    public final NumberPath<Long> taskLimit = createNumber("taskLimit", Long.class);

    public final com.querydsl.sql.PrimaryKey<QAsyncTaskType> asyncTaskTypePk = createPrimaryKey(id);

    public QAsyncTaskType(String variable) {
        super(QAsyncTaskType.class, forVariable(variable), "NDFL_1_0", "ASYNC_TASK_TYPE");
        addMetadata();
    }

    public QAsyncTaskType(String variable, String schema, String table) {
        super(QAsyncTaskType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAsyncTaskType(Path<? extends QAsyncTaskType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "ASYNC_TASK_TYPE");
        addMetadata();
    }

    public QAsyncTaskType(PathMetadata metadata) {
        super(QAsyncTaskType.class, metadata, "NDFL_1_0", "ASYNC_TASK_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(devMode, ColumnMetadata.named("DEV_MODE").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(handlerJndi, ColumnMetadata.named("HANDLER_JNDI").withIndex(3).ofType(Types.VARCHAR).withSize(500).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(limitKind, ColumnMetadata.named("LIMIT_KIND").withIndex(6).ofType(Types.VARCHAR).withSize(400));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(300).notNull());
        addMetadata(shortQueueLimit, ColumnMetadata.named("SHORT_QUEUE_LIMIT").withIndex(4).ofType(Types.DECIMAL).withSize(18));
        addMetadata(taskLimit, ColumnMetadata.named("TASK_LIMIT").withIndex(5).ofType(Types.DECIMAL).withSize(18));
    }

}

