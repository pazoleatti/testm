package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedTreg is a Querydsl query type for QSchedTreg
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedTreg extends com.querydsl.sql.RelationalPathBase<QSchedTreg> {

    private static final long serialVersionUID = 476240506;

    public static final QSchedTreg schedTreg = new QSchedTreg("SCHED_TREG");

    public final StringPath regkey = createString("regkey");

    public final StringPath regvalue = createString("regvalue");

    public final com.querydsl.sql.PrimaryKey<QSchedTreg> sysC0016078 = createPrimaryKey(regkey);

    public QSchedTreg(String variable) {
        super(QSchedTreg.class, forVariable(variable), "NDFL_1_0", "SCHED_TREG");
        addMetadata();
    }

    public QSchedTreg(String variable, String schema, String table) {
        super(QSchedTreg.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedTreg(Path<? extends QSchedTreg> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_TREG");
        addMetadata();
    }

    public QSchedTreg(PathMetadata metadata) {
        super(QSchedTreg.class, metadata, "NDFL_1_0", "SCHED_TREG");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(regkey, ColumnMetadata.named("REGKEY").withIndex(1).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(regvalue, ColumnMetadata.named("REGVALUE").withIndex(2).ofType(Types.VARCHAR).withSize(254));
    }

}

