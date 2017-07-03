package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoUnitsOfMeasure is a Querydsl query type for QSdoUnitsOfMeasure
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoUnitsOfMeasure extends com.querydsl.sql.RelationalPathBase<QSdoUnitsOfMeasure> {

    private static final long serialVersionUID = 1395460409;

    public static final QSdoUnitsOfMeasure sdoUnitsOfMeasure = new QSdoUnitsOfMeasure("SDO_UNITS_OF_MEASURE");

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<java.math.BigInteger> factorB = createNumber("factorB", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> factorC = createNumber("factorC", java.math.BigInteger.class);

    public final StringPath informationSource = createString("informationSource");

    public final StringPath isLegacy = createString("isLegacy");

    public final NumberPath<Long> legacyCode = createNumber("legacyCode", Long.class);

    public final StringPath legacyUnitName = createString("legacyUnitName");

    public final StringPath shortName = createString("shortName");

    public final NumberPath<Long> targetUomId = createNumber("targetUomId", Long.class);

    public final StringPath unitOfMeasName = createString("unitOfMeasName");

    public final StringPath unitOfMeasType = createString("unitOfMeasType");

    public final NumberPath<Long> uomId = createNumber("uomId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoUnitsOfMeasure> unitOfMeasurePrim = createPrimaryKey(uomId);

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> unitOfMeasureForeignUom = createForeignKey(targetUomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoEllipsoids> ellipsoidForeignLegacy = createForeignKey(legacyCode, "ELLIPSOID_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> unitOfMeasureForeignLegacy = createForeignKey(legacyCode, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> _unitOfMeasureForeignUom = createInvForeignKey(uomId, "TARGET_UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoPrimeMeridians> _primeMeridianForeignUom = createInvForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordAxes> _coordAxisForeignUom = createInvForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoEllipsoids> _ellipsoidForeignUom = createInvForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamVals> _coordOpParaValForeignUom = createInvForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> _unitOfMeasureForeignLegacy = createInvForeignKey(uomId, "LEGACY_CODE");

    public QSdoUnitsOfMeasure(String variable) {
        super(QSdoUnitsOfMeasure.class, forVariable(variable), "MDSYS", "SDO_UNITS_OF_MEASURE");
        addMetadata();
    }

    public QSdoUnitsOfMeasure(String variable, String schema, String table) {
        super(QSdoUnitsOfMeasure.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoUnitsOfMeasure(Path<? extends QSdoUnitsOfMeasure> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_UNITS_OF_MEASURE");
        addMetadata();
    }

    public QSdoUnitsOfMeasure(PathMetadata metadata) {
        super(QSdoUnitsOfMeasure.class, metadata, "MDSYS", "SDO_UNITS_OF_MEASURE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(10).ofType(Types.VARCHAR).withSize(40));
        addMetadata(factorB, ColumnMetadata.named("FACTOR_B").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(factorC, ColumnMetadata.named("FACTOR_C").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(9).ofType(Types.VARCHAR).withSize(254));
        addMetadata(isLegacy, ColumnMetadata.named("IS_LEGACY").withIndex(11).ofType(Types.VARCHAR).withSize(5).notNull());
        addMetadata(legacyCode, ColumnMetadata.named("LEGACY_CODE").withIndex(12).ofType(Types.DECIMAL).withSize(10));
        addMetadata(legacyUnitName, ColumnMetadata.named("LEGACY_UNIT_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(80));
        addMetadata(shortName, ColumnMetadata.named("SHORT_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(80));
        addMetadata(targetUomId, ColumnMetadata.named("TARGET_UOM_ID").withIndex(6).ofType(Types.DECIMAL).withSize(10));
        addMetadata(unitOfMeasName, ColumnMetadata.named("UNIT_OF_MEAS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(2083).notNull());
        addMetadata(unitOfMeasType, ColumnMetadata.named("UNIT_OF_MEAS_TYPE").withIndex(5).ofType(Types.VARCHAR).withSize(50));
        addMetadata(uomId, ColumnMetadata.named("UOM_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
    }

}

