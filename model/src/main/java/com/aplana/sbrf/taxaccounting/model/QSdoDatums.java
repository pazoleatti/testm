package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoDatums is a Querydsl query type for QSdoDatums
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoDatums extends com.querydsl.sql.RelationalPathBase<QSdoDatums> {

    private static final long serialVersionUID = -1104934075;

    public static final QSdoDatums sdoDatums = new QSdoDatums("SDO_DATUMS");

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<Long> datumId = createNumber("datumId", Long.class);

    public final StringPath datumName = createString("datumName");

    public final StringPath datumType = createString("datumType");

    public final NumberPath<Long> ellipsoidId = createNumber("ellipsoidId", Long.class);

    public final StringPath informationSource = createString("informationSource");

    public final StringPath isLegacy = createString("isLegacy");

    public final NumberPath<Long> legacyCode = createNumber("legacyCode", Long.class);

    public final NumberPath<Long> primeMeridianId = createNumber("primeMeridianId", Long.class);

    public final NumberPath<java.math.BigInteger> rotateX = createNumber("rotateX", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rotateY = createNumber("rotateY", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rotateZ = createNumber("rotateZ", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> scaleAdjust = createNumber("scaleAdjust", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftX = createNumber("shiftX", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftY = createNumber("shiftY", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftZ = createNumber("shiftZ", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QSdoDatums> datumPrim = createPrimaryKey(datumId);

    public final com.querydsl.sql.ForeignKey<QSdoPrimeMeridians> datumForeignMeridian = createForeignKey(primeMeridianId, "PRIME_MERIDIAN_ID");

    public final com.querydsl.sql.ForeignKey<QSdoEllipsoids> datumForeignEllipsoid = createForeignKey(ellipsoidId, "ELLIPSOID_ID");

    public final com.querydsl.sql.ForeignKey<QSdoDatums> datumForeignLegacy = createForeignKey(legacyCode, "DATUM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignDatum = createInvForeignKey(datumId, "DATUM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoDatums> _datumForeignLegacy = createInvForeignKey(datumId, "LEGACY_CODE");

    public QSdoDatums(String variable) {
        super(QSdoDatums.class, forVariable(variable), "MDSYS", "SDO_DATUMS");
        addMetadata();
    }

    public QSdoDatums(String variable, String schema, String table) {
        super(QSdoDatums.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoDatums(Path<? extends QSdoDatums> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_DATUMS");
        addMetadata();
    }

    public QSdoDatums(PathMetadata metadata) {
        super(QSdoDatums.class, metadata, "MDSYS", "SDO_DATUMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(7).ofType(Types.VARCHAR).withSize(40));
        addMetadata(datumId, ColumnMetadata.named("DATUM_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(datumName, ColumnMetadata.named("DATUM_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(datumType, ColumnMetadata.named("DATUM_TYPE").withIndex(3).ofType(Types.VARCHAR).withSize(24));
        addMetadata(ellipsoidId, ColumnMetadata.named("ELLIPSOID_ID").withIndex(4).ofType(Types.DECIMAL).withSize(10));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(6).ofType(Types.VARCHAR).withSize(254));
        addMetadata(isLegacy, ColumnMetadata.named("IS_LEGACY").withIndex(15).ofType(Types.VARCHAR).withSize(5).notNull());
        addMetadata(legacyCode, ColumnMetadata.named("LEGACY_CODE").withIndex(16).ofType(Types.DECIMAL).withSize(10));
        addMetadata(primeMeridianId, ColumnMetadata.named("PRIME_MERIDIAN_ID").withIndex(5).ofType(Types.DECIMAL).withSize(10));
        addMetadata(rotateX, ColumnMetadata.named("ROTATE_X").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rotateY, ColumnMetadata.named("ROTATE_Y").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rotateZ, ColumnMetadata.named("ROTATE_Z").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(scaleAdjust, ColumnMetadata.named("SCALE_ADJUST").withIndex(14).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftX, ColumnMetadata.named("SHIFT_X").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftY, ColumnMetadata.named("SHIFT_Y").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftZ, ColumnMetadata.named("SHIFT_Z").withIndex(10).ofType(Types.DECIMAL).withSize(22));
    }

}

