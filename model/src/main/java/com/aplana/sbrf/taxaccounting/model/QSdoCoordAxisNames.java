package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordAxisNames is a Querydsl query type for QSdoCoordAxisNames
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordAxisNames extends com.querydsl.sql.RelationalPathBase<QSdoCoordAxisNames> {

    private static final long serialVersionUID = 641734931;

    public static final QSdoCoordAxisNames sdoCoordAxisNames = new QSdoCoordAxisNames("SDO_COORD_AXIS_NAMES");

    public final StringPath coordAxisName = createString("coordAxisName");

    public final NumberPath<Long> coordAxisNameId = createNumber("coordAxisNameId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordAxisNames> coordAxisNamePrim = createPrimaryKey(coordAxisNameId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordAxes> _coordAxisForeignAxis = createInvForeignKey(coordAxisNameId, "COORD_AXIS_NAME_ID");

    public QSdoCoordAxisNames(String variable) {
        super(QSdoCoordAxisNames.class, forVariable(variable), "MDSYS", "SDO_COORD_AXIS_NAMES");
        addMetadata();
    }

    public QSdoCoordAxisNames(String variable, String schema, String table) {
        super(QSdoCoordAxisNames.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordAxisNames(Path<? extends QSdoCoordAxisNames> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_AXIS_NAMES");
        addMetadata();
    }

    public QSdoCoordAxisNames(PathMetadata metadata) {
        super(QSdoCoordAxisNames.class, metadata, "MDSYS", "SDO_COORD_AXIS_NAMES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordAxisName, ColumnMetadata.named("COORD_AXIS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(coordAxisNameId, ColumnMetadata.named("COORD_AXIS_NAME_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
    }

}

