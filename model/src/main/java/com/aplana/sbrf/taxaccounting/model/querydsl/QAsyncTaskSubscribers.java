package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAsyncTaskSubscribers is a Querydsl query type for QAsyncTaskSubscribers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAsyncTaskSubscribers extends com.querydsl.sql.RelationalPathBase<QAsyncTaskSubscribers> {

    private static final long serialVersionUID = -30572618;

    public static final QAsyncTaskSubscribers asyncTaskSubscribers = new QAsyncTaskSubscribers("ASYNC_TASK_SUBSCRIBERS");

    public final NumberPath<Long> asyncTaskId = createNumber("asyncTaskId", Long.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QAsyncTaskSubscribers> asyncTaskSubscribersPk = createPrimaryKey(asyncTaskId, userId);

    public final com.querydsl.sql.ForeignKey<QSecUser> asyncTSubscrFkSecUser = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QAsyncTask> asyncTSubscrFkAsyncTask = createForeignKey(asyncTaskId, "ID");

    public QAsyncTaskSubscribers(String variable) {
        super(QAsyncTaskSubscribers.class, forVariable(variable), "NDFL_UNSTABLE", "ASYNC_TASK_SUBSCRIBERS");
        addMetadata();
    }

    public QAsyncTaskSubscribers(String variable, String schema, String table) {
        super(QAsyncTaskSubscribers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAsyncTaskSubscribers(String variable, String schema) {
        super(QAsyncTaskSubscribers.class, forVariable(variable), schema, "ASYNC_TASK_SUBSCRIBERS");
        addMetadata();
    }

    public QAsyncTaskSubscribers(Path<? extends QAsyncTaskSubscribers> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "ASYNC_TASK_SUBSCRIBERS");
        addMetadata();
    }

    public QAsyncTaskSubscribers(PathMetadata metadata) {
        super(QAsyncTaskSubscribers.class, metadata, "NDFL_UNSTABLE", "ASYNC_TASK_SUBSCRIBERS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(asyncTaskId, ColumnMetadata.named("ASYNC_TASK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

