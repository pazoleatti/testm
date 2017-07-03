package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoPreferredOpsSystem is a Querydsl query type for QSdoPreferredOpsSystem
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoPreferredOpsSystem extends com.querydsl.sql.RelationalPathBase<QSdoPreferredOpsSystem> {

    private static final long serialVersionUID = -812101663;

    public static final QSdoPreferredOpsSystem sdoPreferredOpsSystem = new QSdoPreferredOpsSystem("SDO_PREFERRED_OPS_SYSTEM");

    public final NumberPath<Long> coordOpId = createNumber("coordOpId", Long.class);

    public final NumberPath<Long> sourceSrid = createNumber("sourceSrid", Long.class);

    public final NumberPath<Long> targetSrid = createNumber("targetSrid", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoPreferredOpsSystem> preferredOpsSysPrim = createPrimaryKey(sourceSrid, targetSrid);

    public QSdoPreferredOpsSystem(String variable) {
        super(QSdoPreferredOpsSystem.class, forVariable(variable), "MDSYS", "SDO_PREFERRED_OPS_SYSTEM");
        addMetadata();
    }

    public QSdoPreferredOpsSystem(String variable, String schema, String table) {
        super(QSdoPreferredOpsSystem.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoPreferredOpsSystem(Path<? extends QSdoPreferredOpsSystem> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_PREFERRED_OPS_SYSTEM");
        addMetadata();
    }

    public QSdoPreferredOpsSystem(PathMetadata metadata) {
        super(QSdoPreferredOpsSystem.class, metadata, "MDSYS", "SDO_PREFERRED_OPS_SYSTEM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpId, ColumnMetadata.named("COORD_OP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(sourceSrid, ColumnMetadata.named("SOURCE_SRID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(targetSrid, ColumnMetadata.named("TARGET_SRID").withIndex(3).ofType(Types.DECIMAL).withSize(10).notNull());
    }

}

