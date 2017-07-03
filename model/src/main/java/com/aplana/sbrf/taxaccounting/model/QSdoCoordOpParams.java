package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOpParams is a Querydsl query type for QSdoCoordOpParams
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOpParams extends com.querydsl.sql.RelationalPathBase<QSdoCoordOpParams> {

    private static final long serialVersionUID = 378698843;

    public static final QSdoCoordOpParams sdoCoordOpParams = new QSdoCoordOpParams("SDO_COORD_OP_PARAMS");

    public final StringPath dataSource = createString("dataSource");

    public final StringPath informationSource = createString("informationSource");

    public final NumberPath<Long> parameterId = createNumber("parameterId", Long.class);

    public final StringPath parameterName = createString("parameterName");

    public final StringPath unitOfMeasType = createString("unitOfMeasType");

    public final com.querydsl.sql.PrimaryKey<QSdoCoordOpParams> coordOpParaPrim = createPrimaryKey(parameterId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamUse> _coordOpParaUseForeignPara = createInvForeignKey(parameterId, "PARAMETER_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamVals> _coordOpParaValForeignPara = createInvForeignKey(parameterId, "PARAMETER_ID");

    public QSdoCoordOpParams(String variable) {
        super(QSdoCoordOpParams.class, forVariable(variable), "MDSYS", "SDO_COORD_OP_PARAMS");
        addMetadata();
    }

    public QSdoCoordOpParams(String variable, String schema, String table) {
        super(QSdoCoordOpParams.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOpParams(Path<? extends QSdoCoordOpParams> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OP_PARAMS");
        addMetadata();
    }

    public QSdoCoordOpParams(PathMetadata metadata) {
        super(QSdoCoordOpParams.class, metadata, "MDSYS", "SDO_COORD_OP_PARAMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(4).ofType(Types.VARCHAR).withSize(40));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(3).ofType(Types.VARCHAR).withSize(254));
        addMetadata(parameterId, ColumnMetadata.named("PARAMETER_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(parameterName, ColumnMetadata.named("PARAMETER_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80));
        addMetadata(unitOfMeasType, ColumnMetadata.named("UNIT_OF_MEAS_TYPE").withIndex(5).ofType(Types.VARCHAR).withSize(50));
    }

}

