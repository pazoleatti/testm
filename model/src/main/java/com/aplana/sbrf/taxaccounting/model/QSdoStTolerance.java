package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoStTolerance is a Querydsl query type for QSdoStTolerance
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoStTolerance extends com.querydsl.sql.RelationalPathBase<QSdoStTolerance> {

    private static final long serialVersionUID = 1343770091;

    public static final QSdoStTolerance sdoStTolerance = new QSdoStTolerance("SDO_ST_TOLERANCE");

    public final NumberPath<java.math.BigInteger> tolerance = createNumber("tolerance", java.math.BigInteger.class);

    public QSdoStTolerance(String variable) {
        super(QSdoStTolerance.class, forVariable(variable), "MDSYS", "SDO_ST_TOLERANCE");
        addMetadata();
    }

    public QSdoStTolerance(String variable, String schema, String table) {
        super(QSdoStTolerance.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoStTolerance(Path<? extends QSdoStTolerance> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_ST_TOLERANCE");
        addMetadata();
    }

    public QSdoStTolerance(PathMetadata metadata) {
        super(QSdoStTolerance.class, metadata, "MDSYS", "SDO_ST_TOLERANCE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(tolerance, ColumnMetadata.named("TOLERANCE").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

