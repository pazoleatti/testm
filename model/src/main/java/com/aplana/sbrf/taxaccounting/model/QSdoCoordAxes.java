package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordAxes is a Querydsl query type for QSdoCoordAxes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordAxes extends com.querydsl.sql.RelationalPathBase<QSdoCoordAxes> {

    private static final long serialVersionUID = -262797639;

    public static final QSdoCoordAxes sdoCoordAxes = new QSdoCoordAxes("SDO_COORD_AXES");

    public final StringPath coordAxisAbbreviation = createString("coordAxisAbbreviation");

    public final NumberPath<Long> coordAxisNameId = createNumber("coordAxisNameId", Long.class);

    public final StringPath coordAxisOrientation = createString("coordAxisOrientation");

    public final NumberPath<Long> coordSysId = createNumber("coordSysId", Long.class);

    public final NumberPath<Integer> order = createNumber("order", Integer.class);

    public final NumberPath<Long> uomId = createNumber("uomId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordAxes> coordAxisPrim = createPrimaryKey(coordSysId, order);

    public final com.querydsl.sql.ForeignKey<QSdoCoordAxisNames> coordAxisForeignAxis = createForeignKey(coordAxisNameId, "COORD_AXIS_NAME_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> coordAxisForeignUom = createForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordSys> coordAxisForeignCs = createForeignKey(coordSysId, "COORD_SYS_ID");

    public QSdoCoordAxes(String variable) {
        super(QSdoCoordAxes.class, forVariable(variable), "MDSYS", "SDO_COORD_AXES");
        addMetadata();
    }

    public QSdoCoordAxes(String variable, String schema, String table) {
        super(QSdoCoordAxes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordAxes(Path<? extends QSdoCoordAxes> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_AXES");
        addMetadata();
    }

    public QSdoCoordAxes(PathMetadata metadata) {
        super(QSdoCoordAxes.class, metadata, "MDSYS", "SDO_COORD_AXES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordAxisAbbreviation, ColumnMetadata.named("COORD_AXIS_ABBREVIATION").withIndex(4).ofType(Types.VARCHAR).withSize(24));
        addMetadata(coordAxisNameId, ColumnMetadata.named("COORD_AXIS_NAME_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10));
        addMetadata(coordAxisOrientation, ColumnMetadata.named("COORD_AXIS_ORIENTATION").withIndex(3).ofType(Types.VARCHAR).withSize(24));
        addMetadata(coordSysId, ColumnMetadata.named("COORD_SYS_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(order, ColumnMetadata.named("ORDER").withIndex(6).ofType(Types.DECIMAL).withSize(5).notNull());
        addMetadata(uomId, ColumnMetadata.named("UOM_ID").withIndex(5).ofType(Types.DECIMAL).withSize(10));
    }

}

