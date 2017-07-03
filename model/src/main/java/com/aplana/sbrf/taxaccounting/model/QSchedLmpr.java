package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedLmpr is a Querydsl query type for QSchedLmpr
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedLmpr extends com.querydsl.sql.RelationalPathBase<QSchedLmpr> {

    private static final long serialVersionUID = 475997725;

    public static final QSchedLmpr schedLmpr = new QSchedLmpr("SCHED_LMPR");

    public final StringPath leasename = createString("leasename");

    public final StringPath name = createString("name");

    public final StringPath value = createString("value");

    public QSchedLmpr(String variable) {
        super(QSchedLmpr.class, forVariable(variable), "NDFL_1_0", "SCHED_LMPR");
        addMetadata();
    }

    public QSchedLmpr(String variable, String schema, String table) {
        super(QSchedLmpr.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedLmpr(Path<? extends QSchedLmpr> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_LMPR");
        addMetadata();
    }

    public QSchedLmpr(PathMetadata metadata) {
        super(QSchedLmpr.class, metadata, "NDFL_1_0", "SCHED_LMPR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(leasename, ColumnMetadata.named("LEASENAME").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(254).notNull());
    }

}

