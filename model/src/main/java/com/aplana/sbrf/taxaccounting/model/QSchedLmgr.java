package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedLmgr is a Querydsl query type for QSchedLmgr
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedLmgr extends com.querydsl.sql.RelationalPathBase<QSchedLmgr> {

    private static final long serialVersionUID = 475997446;

    public static final QSchedLmgr schedLmgr = new QSchedLmgr("SCHED_LMGR");

    public final StringPath disabled = createString("disabled");

    public final NumberPath<java.math.BigInteger> leaseExpireTime = createNumber("leaseExpireTime", java.math.BigInteger.class);

    public final StringPath leasename = createString("leasename");

    public final StringPath leaseowner = createString("leaseowner");

    public QSchedLmgr(String variable) {
        super(QSchedLmgr.class, forVariable(variable), "NDFL_1_0", "SCHED_LMGR");
        addMetadata();
    }

    public QSchedLmgr(String variable, String schema, String table) {
        super(QSchedLmgr.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedLmgr(Path<? extends QSchedLmgr> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_LMGR");
        addMetadata();
    }

    public QSchedLmgr(PathMetadata metadata) {
        super(QSchedLmgr.class, metadata, "NDFL_1_0", "SCHED_LMGR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(disabled, ColumnMetadata.named("DISABLED").withIndex(4).ofType(Types.VARCHAR).withSize(254));
        addMetadata(leaseExpireTime, ColumnMetadata.named("LEASE_EXPIRE_TIME").withIndex(3).ofType(Types.DECIMAL).withSize(19));
        addMetadata(leasename, ColumnMetadata.named("LEASENAME").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(leaseowner, ColumnMetadata.named("LEASEOWNER").withIndex(2).ofType(Types.VARCHAR).withSize(254));
    }

}

