package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOpPaths is a Querydsl query type for QSdoCoordOpPaths
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOpPaths extends com.querydsl.sql.RelationalPathBase<QSdoCoordOpPaths> {

    private static final long serialVersionUID = 12218233;

    public static final QSdoCoordOpPaths sdoCoordOpPaths = new QSdoCoordOpPaths("SDO_COORD_OP_PATHS");

    public final NumberPath<Long> concatOperationId = createNumber("concatOperationId", Long.class);

    public final NumberPath<Integer> opPathStep = createNumber("opPathStep", Integer.class);

    public final NumberPath<Long> singleOperationId = createNumber("singleOperationId", Long.class);

    public final NumberPath<Long> singleOpSourceId = createNumber("singleOpSourceId", Long.class);

    public final NumberPath<Long> singleOpTargetId = createNumber("singleOpTargetId", Long.class);

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordOpPathForeignTarget = createForeignKey(singleOpTargetId, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordOpPathForeignSource = createForeignKey(singleOpSourceId, "SRID");

    public QSdoCoordOpPaths(String variable) {
        super(QSdoCoordOpPaths.class, forVariable(variable), "MDSYS", "SDO_COORD_OP_PATHS");
        addMetadata();
    }

    public QSdoCoordOpPaths(String variable, String schema, String table) {
        super(QSdoCoordOpPaths.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOpPaths(Path<? extends QSdoCoordOpPaths> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OP_PATHS");
        addMetadata();
    }

    public QSdoCoordOpPaths(PathMetadata metadata) {
        super(QSdoCoordOpPaths.class, metadata, "MDSYS", "SDO_COORD_OP_PATHS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(concatOperationId, ColumnMetadata.named("CONCAT_OPERATION_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(opPathStep, ColumnMetadata.named("OP_PATH_STEP").withIndex(5).ofType(Types.DECIMAL).withSize(5));
        addMetadata(singleOperationId, ColumnMetadata.named("SINGLE_OPERATION_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10));
        addMetadata(singleOpSourceId, ColumnMetadata.named("SINGLE_OP_SOURCE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(10));
        addMetadata(singleOpTargetId, ColumnMetadata.named("SINGLE_OP_TARGET_ID").withIndex(4).ofType(Types.DECIMAL).withSize(10));
    }

}

