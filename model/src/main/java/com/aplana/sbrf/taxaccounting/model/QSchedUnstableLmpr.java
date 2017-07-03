package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedUnstableLmpr is a Querydsl query type for QSchedUnstableLmpr
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedUnstableLmpr extends com.querydsl.sql.RelationalPathBase<QSchedUnstableLmpr> {

    private static final long serialVersionUID = 2073426257;

    public static final QSchedUnstableLmpr schedUnstableLmpr = new QSchedUnstableLmpr("SCHED_UNSTABLE_LMPR");

    public final StringPath leasename = createString("leasename");

    public final StringPath name = createString("name");

    public final StringPath value = createString("value");

    public QSchedUnstableLmpr(String variable) {
        super(QSchedUnstableLmpr.class, forVariable(variable), "NDFL_1_0", "SCHED_UNSTABLE_LMPR");
        addMetadata();
    }

    public QSchedUnstableLmpr(String variable, String schema, String table) {
        super(QSchedUnstableLmpr.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedUnstableLmpr(Path<? extends QSchedUnstableLmpr> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_UNSTABLE_LMPR");
        addMetadata();
    }

    public QSchedUnstableLmpr(PathMetadata metadata) {
        super(QSchedUnstableLmpr.class, metadata, "NDFL_1_0", "SCHED_UNSTABLE_LMPR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(leasename, ColumnMetadata.named("LEASENAME").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(254).notNull());
    }

}

