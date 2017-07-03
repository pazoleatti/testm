package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOlapSessionDims is a Querydsl query type for QOlapSessionDims
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOlapSessionDims extends com.querydsl.sql.RelationalPathBase<QOlapSessionDims> {

    private static final long serialVersionUID = 757430136;

    public static final QOlapSessionDims olapSessionDims = new QOlapSessionDims("OLAP_SESSION_DIMS");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath versionId = createString("versionId");

    public QOlapSessionDims(String variable) {
        super(QOlapSessionDims.class, forVariable(variable), "OLAPSYS", "OLAP_SESSION_DIMS");
        addMetadata();
    }

    public QOlapSessionDims(String variable, String schema, String table) {
        super(QOlapSessionDims.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOlapSessionDims(Path<? extends QOlapSessionDims> path) {
        super(path.getType(), path.getMetadata(), "OLAPSYS", "OLAP_SESSION_DIMS");
        addMetadata();
    }

    public QOlapSessionDims(PathMetadata metadata) {
        super(QOlapSessionDims.class, metadata, "OLAPSYS", "OLAP_SESSION_DIMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(versionId, ColumnMetadata.named("VERSION_ID").withIndex(2).ofType(Types.CHAR).withSize(4));
    }

}

