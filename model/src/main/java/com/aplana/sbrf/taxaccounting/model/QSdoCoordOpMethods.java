package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordOpMethods is a Querydsl query type for QSdoCoordOpMethods
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordOpMethods extends com.querydsl.sql.RelationalPathBase<QSdoCoordOpMethods> {

    private static final long serialVersionUID = 603792253;

    public static final QSdoCoordOpMethods sdoCoordOpMethods = new QSdoCoordOpMethods("SDO_COORD_OP_METHODS");

    public final NumberPath<Long> coordOpMethodId = createNumber("coordOpMethodId", Long.class);

    public final StringPath coordOpMethodName = createString("coordOpMethodName");

    public final StringPath dataSource = createString("dataSource");

    public final StringPath informationSource = createString("informationSource");

    public final NumberPath<Byte> isImplementedForward = createNumber("isImplementedForward", Byte.class);

    public final NumberPath<Byte> isImplementedReverse = createNumber("isImplementedReverse", Byte.class);

    public final StringPath legacyName = createString("legacyName");

    public final NumberPath<Byte> reverseOp = createNumber("reverseOp", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QSdoCoordOpMethods> coordOpMethodPrim = createPrimaryKey(coordOpMethodId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> _coordOperationForeignMethod = createInvForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamUse> _coordOpParaUseForeignMeth = createInvForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpParamVals> _coordOpParaValForeignMeth = createInvForeignKey(coordOpMethodId, "COORD_OP_METHOD_ID");

    public QSdoCoordOpMethods(String variable) {
        super(QSdoCoordOpMethods.class, forVariable(variable), "MDSYS", "SDO_COORD_OP_METHODS");
        addMetadata();
    }

    public QSdoCoordOpMethods(String variable, String schema, String table) {
        super(QSdoCoordOpMethods.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordOpMethods(Path<? extends QSdoCoordOpMethods> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_OP_METHODS");
        addMetadata();
    }

    public QSdoCoordOpMethods(PathMetadata metadata) {
        super(QSdoCoordOpMethods.class, metadata, "MDSYS", "SDO_COORD_OP_METHODS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordOpMethodId, ColumnMetadata.named("COORD_OP_METHOD_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(coordOpMethodName, ColumnMetadata.named("COORD_OP_METHOD_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(50));
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(6).ofType(Types.VARCHAR).withSize(40));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(5).ofType(Types.VARCHAR).withSize(254));
        addMetadata(isImplementedForward, ColumnMetadata.named("IS_IMPLEMENTED_FORWARD").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(isImplementedReverse, ColumnMetadata.named("IS_IMPLEMENTED_REVERSE").withIndex(8).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(legacyName, ColumnMetadata.named("LEGACY_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(50));
        addMetadata(reverseOp, ColumnMetadata.named("REVERSE_OP").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

