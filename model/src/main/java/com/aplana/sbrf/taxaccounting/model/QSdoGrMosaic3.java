package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGrMosaic3 is a Querydsl query type for QSdoGrMosaic3
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGrMosaic3 extends com.querydsl.sql.RelationalPathBase<QSdoGrMosaic3> {

    private static final long serialVersionUID = 1849332861;

    public static final QSdoGrMosaic3 sdoGrMosaic3 = new QSdoGrMosaic3("SDO_GR_MOSAIC_3");

    public final NumberPath<java.math.BigInteger> p = createNumber("p", java.math.BigInteger.class);

    public QSdoGrMosaic3(String variable) {
        super(QSdoGrMosaic3.class, forVariable(variable), "MDSYS", "SDO_GR_MOSAIC_3");
        addMetadata();
    }

    public QSdoGrMosaic3(String variable, String schema, String table) {
        super(QSdoGrMosaic3.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGrMosaic3(Path<? extends QSdoGrMosaic3> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GR_MOSAIC_3");
        addMetadata();
    }

    public QSdoGrMosaic3(PathMetadata metadata) {
        super(QSdoGrMosaic3.class, metadata, "MDSYS", "SDO_GR_MOSAIC_3");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(p, ColumnMetadata.named("P").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

