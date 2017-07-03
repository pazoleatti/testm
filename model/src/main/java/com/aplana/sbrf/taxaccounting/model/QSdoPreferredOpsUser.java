package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoPreferredOpsUser is a Querydsl query type for QSdoPreferredOpsUser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoPreferredOpsUser extends com.querydsl.sql.RelationalPathBase<QSdoPreferredOpsUser> {

    private static final long serialVersionUID = -854422019;

    public static final QSdoPreferredOpsUser sdoPreferredOpsUser = new QSdoPreferredOpsUser("SDO_PREFERRED_OPS_USER");

    public final NumberPath<Long> coordOpId = createNumber("coordOpId", Long.class);

    public final NumberPath<Long> sourceSrid = createNumber("sourceSrid", Long.class);

    public final NumberPath<Long> targetSrid = createNumber("targetSrid", Long.class);

    public final StringPath useCase = createString("useCase");

    public final com.querydsl.sql.PrimaryKey<QSdoPreferredOpsUser> preferredOpsUsePrim = createPrimaryKey(sourceSrid, targetSrid, useCase);

    public QSdoPreferredOpsUser(String variable) {
        super(QSdoPreferredOpsUser.class, forVariable(variable), "MDSYS", "SDO_PREFERRED_OPS_USER");
        addMetadata();
    }

    public QSdoPreferredOpsUser(String variable, String schema, String table) {
        super(QSdoPreferredOpsUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoPreferredOpsUser(Path<? extends QSdoPreferredOpsUser> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_PREFERRED_OPS_USER");
        addMetadata();
    }

    public QSdoPreferredOpsUser(PathMetadata metadata) {
        super(QSdoPreferredOpsUser.class, metadata, "MDSYS", "SDO_PREFERRED_OPS_USER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpId, ColumnMetadata.named("COORD_OP_ID").withIndex(3).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(sourceSrid, ColumnMetadata.named("SOURCE_SRID").withIndex(2).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(targetSrid, ColumnMetadata.named("TARGET_SRID").withIndex(4).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(useCase, ColumnMetadata.named("USE_CASE").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

