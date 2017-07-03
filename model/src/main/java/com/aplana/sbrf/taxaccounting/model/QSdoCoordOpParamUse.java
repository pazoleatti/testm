package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOpParamUse is a Querydsl query type for QSdoCoordOpParamUse
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOpParamUse extends com.querydsl.sql.RelationalPathBase<QSdoCoordOpParamUse> {

    private static final long serialVersionUID = -1142657201;

    public static final QSdoCoordOpParamUse sdoCoordOpParamUse = new QSdoCoordOpParamUse("SDO_COORD_OP_PARAM_USE");

    public final NumberPath<Long> coordOpMethodId = createNumber("coordOpMethodId", Long.class);

    public final StringPath legacyParamName = createString("legacyParamName");

    public final NumberPath<Long> parameterId = createNumber("parameterId", Long.class);

    public final StringPath paramSignReversal = createString("paramSignReversal");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordOpParamUse> coordOpParaUsePrim = createPrimaryKey(coordOpMethodId, sortOrder);

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpMethods> coordOpParaUseForeignMeth = createForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParams> coordOpParaUseForeignPara = createForeignKey(parameterId, "PARAMETER_ID");

    public QSdoCoordOpParamUse(String variable) {
        super(QSdoCoordOpParamUse.class, forVariable(variable), "MDSYS", "SDO_COORD_OP_PARAM_USE");
        addMetadata();
    }

    public QSdoCoordOpParamUse(String variable, String schema, String table) {
        super(QSdoCoordOpParamUse.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOpParamUse(Path<? extends QSdoCoordOpParamUse> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OP_PARAM_USE");
        addMetadata();
    }

    public QSdoCoordOpParamUse(PathMetadata metadata) {
        super(QSdoCoordOpParamUse.class, metadata, "MDSYS", "SDO_COORD_OP_PARAM_USE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpMethodId, ColumnMetadata.named("COORD_OP_METHOD_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(legacyParamName, ColumnMetadata.named("LEGACY_PARAM_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(80));
        addMetadata(parameterId, ColumnMetadata.named("PARAMETER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10));
        addMetadata(paramSignReversal, ColumnMetadata.named("PARAM_SIGN_REVERSAL").withIndex(5).ofType(Types.VARCHAR).withSize(3));
        addMetadata(sortOrder, ColumnMetadata.named("SORT_ORDER").withIndex(4).ofType(Types.DECIMAL).withSize(5).notNull());
    }

}

