package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLockData is a Querydsl query type for QLockData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLockData extends com.querydsl.sql.RelationalPathBase<QLockData> {

    private static final long serialVersionUID = 1929419666;

    public static final QLockData lockData = new QLockData("LOCK_DATA");

    public final DateTimePath<org.joda.time.LocalDateTime> dateLock = createDateTime("dateLock", org.joda.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath key = createString("key");

    public final NumberPath<Long> taskId = createNumber("taskId", Long.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QLockData> sysC00658009 = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSecUser> lockDataFkUserId = createForeignKey(userId, "ID");

    public QLockData(String variable) {
        super(QLockData.class, forVariable(variable), "NDFL_UNSTABLE", "LOCK_DATA");
        addMetadata();
    }

    public QLockData(String variable, String schema, String table) {
        super(QLockData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLockData(Path<? extends QLockData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOCK_DATA");
        addMetadata();
    }

    public QLockData(PathMetadata metadata) {
        super(QLockData.class, metadata, "NDFL_UNSTABLE", "LOCK_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateLock, ColumnMetadata.named("DATE_LOCK").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(key, ColumnMetadata.named("KEY").withIndex(1).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(taskId, ColumnMetadata.named("TASK_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18));
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

