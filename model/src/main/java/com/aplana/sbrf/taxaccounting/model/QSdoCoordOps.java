package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOps is a Querydsl query type for QSdoCoordOps
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOps extends com.querydsl.sql.RelationalPathBase<QSdoCoordOps> {

    private static final long serialVersionUID = 961367198;

    public static final QSdoCoordOps sdoCoordOps = new QSdoCoordOps("SDO_COORD_OPS");

    public final NumberPath<Long> coordOpId = createNumber("coordOpId", Long.class);

    public final NumberPath<Long> coordOpMethodId = createNumber("coordOpMethodId", Long.class);

    public final StringPath coordOpName = createString("coordOpName");

    public final StringPath coordOpType = createString("coordOpType");

    public final NumberPath<Integer> coordOpVariant = createNumber("coordOpVariant", Integer.class);

    public final StringPath coordTfmVersion = createString("coordTfmVersion");

    public final StringPath dataSource = createString("dataSource");

    public final StringPath informationSource = createString("informationSource");

    public final NumberPath<Byte> isImplementedForward = createNumber("isImplementedForward", Byte.class);

    public final NumberPath<Byte> isImplementedReverse = createNumber("isImplementedReverse", Byte.class);

    public final StringPath isLegacy = createString("isLegacy");

    public final NumberPath<Long> legacyCode = createNumber("legacyCode", Long.class);

    public final NumberPath<Byte> reverseOp = createNumber("reverseOp", Byte.class);

    public final NumberPath<Short> showOperation = createNumber("showOperation", Short.class);

    public final NumberPath<Long> sourceSrid = createNumber("sourceSrid", Long.class);

    public final NumberPath<Long> targetSrid = createNumber("targetSrid", Long.class);

    public final NumberPath<Long> uomIdSourceOffsets = createNumber("uomIdSourceOffsets", Long.class);

    public final NumberPath<Long> uomIdTargetOffsets = createNumber("uomIdTargetOffsets", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordOps> coordOpPrim = createPrimaryKey(coordOpId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpMethods> coordOperationForeignMethod = createForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> coordOperationForeignLegacy = createForeignKey(legacyCode, "COORD_OP_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordOperationForeignSource = createForeignKey(sourceSrid, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordOperationForeignTarget = createForeignKey(targetSrid, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignProj = createInvForeignKey(coordOpId, "PROJECTION_CONV_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamVals> _coordOpParaValForeignOp = createInvForeignKey(coordOpId, "COORD_OP_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> _coordOperationForeignLegacy = createInvForeignKey(coordOpId, "LEGACY_CODE");

    public QSdoCoordOps(String variable) {
        super(QSdoCoordOps.class, forVariable(variable), "MDSYS", "SDO_COORD_OPS");
        addMetadata();
    }

    public QSdoCoordOps(String variable, String schema, String table) {
        super(QSdoCoordOps.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOps(Path<? extends QSdoCoordOps> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OPS");
        addMetadata();
    }

    public QSdoCoordOps(PathMetadata metadata) {
        super(QSdoCoordOps.class, metadata, "MDSYS", "SDO_COORD_OPS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpId, ColumnMetadata.named("COORD_OP_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(coordOpMethodId, ColumnMetadata.named("COORD_OP_METHOD_ID").withIndex(8).ofType(Types.DECIMAL).withSize(10));
        addMetadata(coordOpName, ColumnMetadata.named("COORD_OP_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80));
        addMetadata(coordOpType, ColumnMetadata.named("COORD_OP_TYPE").withIndex(3).ofType(Types.VARCHAR).withSize(24));
        addMetadata(coordOpVariant, ColumnMetadata.named("COORD_OP_VARIANT").withIndex(7).ofType(Types.DECIMAL).withSize(5));
        addMetadata(coordTfmVersion, ColumnMetadata.named("COORD_TFM_VERSION").withIndex(6).ofType(Types.VARCHAR).withSize(24));
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(12).ofType(Types.VARCHAR).withSize(40));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(11).ofType(Types.VARCHAR).withSize(254));
        addMetadata(isImplementedForward, ColumnMetadata.named("IS_IMPLEMENTED_FORWARD").withIndex(17).ofType(Types.DECIMAL).withSize(1));
        addMetadata(isImplementedReverse, ColumnMetadata.named("IS_IMPLEMENTED_REVERSE").withIndex(18).ofType(Types.DECIMAL).withSize(1));
        addMetadata(isLegacy, ColumnMetadata.named("IS_LEGACY").withIndex(14).ofType(Types.VARCHAR).withSize(5).notNull());
        addMetadata(legacyCode, ColumnMetadata.named("LEGACY_CODE").withIndex(15).ofType(Types.DECIMAL).withSize(10));
        addMetadata(reverseOp, ColumnMetadata.named("REVERSE_OP").withIndex(16).ofType(Types.DECIMAL).withSize(1));
        addMetadata(showOperation, ColumnMetadata.named("SHOW_OPERATION").withIndex(13).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(sourceSrid, ColumnMetadata.named("SOURCE_SRID").withIndex(4).ofType(Types.DECIMAL).withSize(10));
        addMetadata(targetSrid, ColumnMetadata.named("TARGET_SRID").withIndex(5).ofType(Types.DECIMAL).withSize(10));
        addMetadata(uomIdSourceOffsets, ColumnMetadata.named("UOM_ID_SOURCE_OFFSETS").withIndex(9).ofType(Types.DECIMAL).withSize(10));
        addMetadata(uomIdTargetOffsets, ColumnMetadata.named("UOM_ID_TARGET_OFFSETS").withIndex(10).ofType(Types.DECIMAL).withSize(10));
    }

}

