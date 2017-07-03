package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedUnstableLmgr is a Querydsl query type for QSchedUnstableLmgr
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedUnstableLmgr extends com.querydsl.sql.RelationalPathBase<QSchedUnstableLmgr> {

    private static final long serialVersionUID = 2073425978;

    public static final QSchedUnstableLmgr schedUnstableLmgr = new QSchedUnstableLmgr("SCHED_UNSTABLE_LMGR");

    public final StringPath disabled = createString("disabled");

    public final NumberPath<java.math.BigInteger> leaseExpireTime = createNumber("leaseExpireTime", java.math.BigInteger.class);

    public final StringPath leasename = createString("leasename");

    public final StringPath leaseowner = createString("leaseowner");

    public final com.querydsl.sql.PrimaryKey<QSchedUnstableLmgr> sysC0016905 = createPrimaryKey(leasename);

    public QSchedUnstableLmgr(String variable) {
        super(QSchedUnstableLmgr.class, forVariable(variable), "NDFL_1_0", "SCHED_UNSTABLE_LMGR");
        addMetadata();
    }

    public QSchedUnstableLmgr(String variable, String schema, String table) {
        super(QSchedUnstableLmgr.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedUnstableLmgr(Path<? extends QSchedUnstableLmgr> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_UNSTABLE_LMGR");
        addMetadata();
    }

    public QSchedUnstableLmgr(PathMetadata metadata) {
        super(QSchedUnstableLmgr.class, metadata, "NDFL_1_0", "SCHED_UNSTABLE_LMGR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(disabled, ColumnMetadata.named("DISABLED").withIndex(4).ofType(Types.VARCHAR).withSize(254));
        addMetadata(leaseExpireTime, ColumnMetadata.named("LEASE_EXPIRE_TIME").withIndex(3).ofType(Types.DECIMAL).withSize(19));
        addMetadata(leasename, ColumnMetadata.named("LEASENAME").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(leaseowner, ColumnMetadata.named("LEASEOWNER").withIndex(2).ofType(Types.VARCHAR).withSize(254));
    }

}

