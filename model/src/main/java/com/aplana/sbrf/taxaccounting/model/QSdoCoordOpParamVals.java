package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOpParamVals is a Querydsl query type for QSdoCoordOpParamVals
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOpParamVals extends com.querydsl.sql.RelationalPathBase<QSdoCoordOpParamVals> {

    private static final long serialVersionUID = -1062622038;

    public static final QSdoCoordOpParamVals sdoCoordOpParamVals = new QSdoCoordOpParamVals("SDO_COORD_OP_PARAM_VALS");

    public final NumberPath<Long> coordOpId = createNumber("coordOpId", Long.class);

    public final NumberPath<Long> coordOpMethodId = createNumber("coordOpMethodId", Long.class);

    public final NumberPath<Long> parameterId = createNumber("parameterId", Long.class);

    public final NumberPath<Float> parameterValue = createNumber("parameterValue", Float.class);

    public final StringPath paramValueFile = createString("paramValueFile");

    public final StringPath paramValueFileRef = createString("paramValueFileRef");

    public final SimplePath<Object> paramValueXml = createSimple("paramValueXml", Object.class);

    public final NumberPath<Long> uomId = createNumber("uomId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordOpParamVals> coordOpParaValPrim = createPrimaryKey(coordOpId, parameterId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> coordOpParaValForeignOp = createForeignKey(coordOpId, "COORD_OP_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParams> coordOpParaValForeignPara = createForeignKey(parameterId, "PARAMETER_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpMethods> coordOpParaValForeignMeth = createForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> coordOpParaValForeignUom = createForeignKey(uomId, "UOM_ID");

    public QSdoCoordOpParamVals(String variable) {
        super(QSdoCoordOpParamVals.class, forVariable(variable), "MDSYS", "SDO_COORD_OP_PARAM_VALS");
        addMetadata();
    }

    public QSdoCoordOpParamVals(String variable, String schema, String table) {
        super(QSdoCoordOpParamVals.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOpParamVals(Path<? extends QSdoCoordOpParamVals> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OP_PARAM_VALS");
        addMetadata();
    }

    public QSdoCoordOpParamVals(PathMetadata metadata) {
        super(QSdoCoordOpParamVals.class, metadata, "MDSYS", "SDO_COORD_OP_PARAM_VALS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpId, ColumnMetadata.named("COORD_OP_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(coordOpMethodId, ColumnMetadata.named("COORD_OP_METHOD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10));
        addMetadata(parameterId, ColumnMetadata.named("PARAMETER_ID").withIndex(3).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(parameterValue, ColumnMetadata.named("PARAMETER_VALUE").withIndex(4).ofType(Types.FLOAT).withSize(49));
        addMetadata(paramValueFile, ColumnMetadata.named("PARAM_VALUE_FILE").withIndex(6).ofType(Types.CLOB).withSize(4000));
        addMetadata(paramValueFileRef, ColumnMetadata.named("PARAM_VALUE_FILE_REF").withIndex(5).ofType(Types.VARCHAR).withSize(254));
        addMetadata(paramValueXml, ColumnMetadata.named("PARAM_VALUE_XML").withIndex(7).ofType(2007).withSize(2000));
        addMetadata(uomId, ColumnMetadata.named("UOM_ID").withIndex(8).ofType(Types.DECIMAL).withSize(10));
    }

}

