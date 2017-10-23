package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAsyncTask is a Querydsl query type for QAsyncTask
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAsyncTask extends com.querydsl.sql.RelationalPathBase<QAsyncTask> {

    private static final long serialVersionUID = -1208731115;

    public static final QAsyncTask asyncTask = new QAsyncTask("ASYNC_TASK");

    public final DateTimePath<org.joda.time.LocalDateTime> createDate = createDateTime("createDate", org.joda.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath node = createString("node");

    public final StringPath priorityNode = createString("priorityNode");

    public final NumberPath<Byte> queue = createNumber("queue", Byte.class);

    public final SimplePath<java.io.InputStream> serializedParams = createSimple("serializedParams", java.io.InputStream.class);

    public final DateTimePath<org.joda.time.LocalDateTime> startProcessDate = createDateTime("startProcessDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> state = createNumber("state", Integer.class);

    public final DateTimePath<org.joda.time.LocalDateTime> stateDate = createDateTime("stateDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QAsyncTask> asyncTaskPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAsyncTaskType> asyncTaskFkType = createForeignKey(typeId, "ID");

    public final com.querydsl.sql.ForeignKey<QAsyncTaskSubscribers> _asyncTSubscrFkAsyncTask = createInvForeignKey(id, "ASYNC_TASK_ID");

    public QAsyncTask(String variable) {
        super(QAsyncTask.class, forVariable(variable), "NDFL_UNSTABLE", "ASYNC_TASK");
        addMetadata();
    }

    public QAsyncTask(String variable, String schema, String table) {
        super(QAsyncTask.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAsyncTask(String variable, String schema) {
        super(QAsyncTask.class, forVariable(variable), schema, "ASYNC_TASK");
        addMetadata();
    }

    public QAsyncTask(Path<? extends QAsyncTask> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "ASYNC_TASK");
        addMetadata();
    }

    public QAsyncTask(PathMetadata metadata) {
        super(QAsyncTask.class, metadata, "NDFL_UNSTABLE", "ASYNC_TASK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createDate, ColumnMetadata.named("CREATE_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(12).ofType(Types.VARCHAR).withSize(400));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(node, ColumnMetadata.named("NODE").withIndex(5).ofType(Types.VARCHAR).withSize(500));
        addMetadata(priorityNode, ColumnMetadata.named("PRIORITY_NODE").withIndex(11).ofType(Types.VARCHAR).withSize(500));
        addMetadata(queue, ColumnMetadata.named("QUEUE").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(serializedParams, ColumnMetadata.named("SERIALIZED_PARAMS").withIndex(7).ofType(Types.BLOB).withSize(4000));
        addMetadata(startProcessDate, ColumnMetadata.named("START_PROCESS_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(9).ofType(Types.DECIMAL).withSize(6).notNull());
        addMetadata(stateDate, ColumnMetadata.named("STATE_DATE").withIndex(10).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(8).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

