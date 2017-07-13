package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLockDataSubscribers is a Querydsl query type for QLockDataSubscribers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLockDataSubscribers extends com.querydsl.sql.RelationalPathBase<QLockDataSubscribers> {

    private static final long serialVersionUID = 889732633;

    public static final QLockDataSubscribers lockDataSubscribers = new QLockDataSubscribers("LOCK_DATA_SUBSCRIBERS");

    public final StringPath lockKey = createString("lockKey");

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QLockDataSubscribers> lockDataSubscribersPk = createPrimaryKey(lockKey, userId);

    public final com.querydsl.sql.ForeignKey<QSecUser> lockDataSubscrFkSecUser = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QLockData> lockDataSubscrFkLockData = createForeignKey(lockKey, "KEY");

    public QLockDataSubscribers(String variable) {
        super(QLockDataSubscribers.class, forVariable(variable), "NDFL_UNSTABLE", "LOCK_DATA_SUBSCRIBERS");
        addMetadata();
    }

    public QLockDataSubscribers(String variable, String schema, String table) {
        super(QLockDataSubscribers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLockDataSubscribers(Path<? extends QLockDataSubscribers> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOCK_DATA_SUBSCRIBERS");
        addMetadata();
    }

    public QLockDataSubscribers(PathMetadata metadata) {
        super(QLockDataSubscribers.class, metadata, "NDFL_UNSTABLE", "LOCK_DATA_SUBSCRIBERS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(lockKey, ColumnMetadata.named("LOCK_KEY").withIndex(1).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

