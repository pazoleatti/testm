package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedUnstableTreg is a Querydsl query type for QSchedUnstableTreg
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedUnstableTreg extends com.querydsl.sql.RelationalPathBase<QSchedUnstableTreg> {

    private static final long serialVersionUID = 2073669038;

    public static final QSchedUnstableTreg schedUnstableTreg = new QSchedUnstableTreg("SCHED_UNSTABLE_TREG");

    public final StringPath regkey = createString("regkey");

    public final StringPath regvalue = createString("regvalue");

    public final com.querydsl.sql.PrimaryKey<QSchedUnstableTreg> sysC0016903 = createPrimaryKey(regkey);

    public QSchedUnstableTreg(String variable) {
        super(QSchedUnstableTreg.class, forVariable(variable), "NDFL_1_0", "SCHED_UNSTABLE_TREG");
        addMetadata();
    }

    public QSchedUnstableTreg(String variable, String schema, String table) {
        super(QSchedUnstableTreg.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedUnstableTreg(Path<? extends QSchedUnstableTreg> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_UNSTABLE_TREG");
        addMetadata();
    }

    public QSchedUnstableTreg(PathMetadata metadata) {
        super(QSchedUnstableTreg.class, metadata, "NDFL_1_0", "SCHED_UNSTABLE_TREG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(regkey, ColumnMetadata.named("REGKEY").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(regvalue, ColumnMetadata.named("REGVALUE").withIndex(2).ofType(Types.VARCHAR).withSize(254));
    }

}

