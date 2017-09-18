package com.aplana.sbrf.taxaccounting.model;

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

    private static final long serialVersionUID = -1069739612;

    public static final QAsyncTask asyncTask = new QAsyncTask("ASYNC_TASK");

    public final NumberPath<Byte> balancingVariant = createNumber("balancingVariant", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> createDate = createDateTime("createDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath node = createString("node");

    public final SimplePath<java.io.InputStream> serializedParams = createSimple("serializedParams", java.io.InputStream.class);

    public final DateTimePath<org.joda.time.LocalDateTime> startProcessDate = createDateTime("startProcessDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QAsyncTask> asyncTaskPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAsyncTaskType> asyncTaskFkType = createForeignKey(typeId, "ID");

    public QAsyncTask(String variable) {
        super(QAsyncTask.class, forVariable(variable), "NDFL_UNSTABLE", "ASYNC_TASK");
        addMetadata();
    }

    public QAsyncTask(String variable, String schema, String table) {
        super(QAsyncTask.class, forVariable(variable), schema, table);
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
        addMetadata(balancingVariant, ColumnMetadata.named("BALANCING_VARIANT").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(createDate, ColumnMetadata.named("CREATE_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(node, ColumnMetadata.named("NODE").withIndex(5).ofType(Types.VARCHAR).withSize(500));
        addMetadata(serializedParams, ColumnMetadata.named("SERIALIZED_PARAMS").withIndex(7).ofType(Types.BLOB).withSize(4000));
        addMetadata(startProcessDate, ColumnMetadata.named("START_PROCESS_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

