package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoEllipsoids is a Querydsl query type for QSdoEllipsoids
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoEllipsoids extends com.querydsl.sql.RelationalPathBase<QSdoEllipsoids> {

    private static final long serialVersionUID = 1235975857;

    public static final QSdoEllipsoids sdoEllipsoids = new QSdoEllipsoids("SDO_ELLIPSOIDS");

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<java.math.BigInteger> ellipsoidId = createNumber("ellipsoidId", java.math.BigInteger.class);

    public final StringPath ellipsoidName = createString("ellipsoidName");

    public final StringPath informationSource = createString("informationSource");

    public final NumberPath<java.math.BigInteger> invFlattening = createNumber("invFlattening", java.math.BigInteger.class);

    public final StringPath isLegacy = createString("isLegacy");

    public final NumberPath<java.math.BigInteger> legacyCode = createNumber("legacyCode", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> semiMajorAxis = createNumber("semiMajorAxis", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> semiMinorAxis = createNumber("semiMinorAxis", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> uomId = createNumber("uomId", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QSdoEllipsoids> ellipsoidPrim = createPrimaryKey(ellipsoidId);

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> ellipsoidForeignUom = createForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoDatums> _datumForeignEllipsoid = createInvForeignKey(ellipsoidId, "ELLIPSOID_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> _ellipsoidForeignLegacy = createInvForeignKey(ellipsoidId, "LEGACY_CODE");

    public QSdoEllipsoids(String variable) {
        super(QSdoEllipsoids.class, forVariable(variable), "MDSYS", "SDO_ELLIPSOIDS");
        addMetadata();
    }

    public QSdoEllipsoids(String variable, String schema, String table) {
        super(QSdoEllipsoids.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoEllipsoids(Path<? extends QSdoEllipsoids> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_ELLIPSOIDS");
        addMetadata();
    }

    public QSdoEllipsoids(PathMetadata metadata) {
        super(QSdoEllipsoids.class, metadata, "MDSYS", "SDO_ELLIPSOIDS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(8).ofType(Types.VARCHAR).withSize(40));
        addMetadata(ellipsoidId, ColumnMetadata.named("ELLIPSOID_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(ellipsoidName, ColumnMetadata.named("ELLIPSOID_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(7).ofType(Types.VARCHAR).withSize(254));
        addMetadata(invFlattening, ColumnMetadata.named("INV_FLATTENING").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(isLegacy, ColumnMetadata.named("IS_LEGACY").withIndex(9).ofType(Types.VARCHAR).withSize(5).notNull());
        addMetadata(legacyCode, ColumnMetadata.named("LEGACY_CODE").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(semiMajorAxis, ColumnMetadata.named("SEMI_MAJOR_AXIS").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(semiMinorAxis, ColumnMetadata.named("SEMI_MINOR_AXIS").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(uomId, ColumnMetadata.named("UOM_ID").withIndex(4).ofType(Types.DECIMAL).withSize(22));
    }

}

