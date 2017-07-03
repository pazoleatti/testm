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

    public final DateTimePath<org.joda.time.DateTime> dateLock = createDateTime("dateLock", org.joda.time.DateTime.class);

    public final StringPath description = createString("description");

    public final StringPath key = createString("key");

    public final NumberPath<Integer> queue = createNumber("queue", Integer.class);

    public final StringPath serverNode = createString("serverNode");

    public final StringPath state = createString("state");

    public final DateTimePath<org.joda.time.DateTime> stateDate = createDateTime("stateDate", org.joda.time.DateTime.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QLockData> lockDataPk = createPrimaryKey(key);

    public final com.querydsl.sql.ForeignKey<QSecUser> lockDataFkUserId = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QLockDataSubscribers> _lockDataSubscrFkLockData = createInvForeignKey(key, "LOCK_KEY");

    public QLockData(String variable) {
        super(QLockData.class, forVariable(variable), "NDFL_1_0", "LOCK_DATA");
        addMetadata();
    }

    public QLockData(String variable, String schema, String table) {
        super(QLockData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLockData(Path<? extends QLockData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "LOCK_DATA");
        addMetadata();
    }

    public QLockData(PathMetadata metadata) {
        super(QLockData.class, metadata, "NDFL_1_0", "LOCK_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateLock, ColumnMetadata.named("DATE_LOCK").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(6).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(key, ColumnMetadata.named("KEY").withIndex(1).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(queue, ColumnMetadata.named("QUEUE").withIndex(7).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(serverNode, ColumnMetadata.named("SERVER_NODE").withIndex(8).ofType(Types.VARCHAR).withSize(100));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(4).ofType(Types.VARCHAR).withSize(500));
        addMetadata(stateDate, ColumnMetadata.named("STATE_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

