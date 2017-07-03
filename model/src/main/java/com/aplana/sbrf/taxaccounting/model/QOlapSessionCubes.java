package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOlapSessionCubes is a Querydsl query type for QOlapSessionCubes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOlapSessionCubes extends com.querydsl.sql.RelationalPathBase<QOlapSessionCubes> {

    private static final long serialVersionUID = 2004920817;

    public static final QOlapSessionCubes olapSessionCubes = new QOlapSessionCubes("OLAP_SESSION_CUBES");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath versionId = createString("versionId");

    public QOlapSessionCubes(String variable) {
        super(QOlapSessionCubes.class, forVariable(variable), "OLAPSYS", "OLAP_SESSION_CUBES");
        addMetadata();
    }

    public QOlapSessionCubes(String variable, String schema, String table) {
        super(QOlapSessionCubes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOlapSessionCubes(Path<? extends QOlapSessionCubes> path) {
        super(path.getType(), path.getMetadata(), "OLAPSYS", "OLAP_SESSION_CUBES");
        addMetadata();
    }

    public QOlapSessionCubes(PathMetadata metadata) {
        super(QOlapSessionCubes.class, metadata, "OLAPSYS", "OLAP_SESSION_CUBES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(versionId, ColumnMetadata.named("VERSION_ID").withIndex(2).ofType(Types.CHAR).withSize(4));
    }

}

