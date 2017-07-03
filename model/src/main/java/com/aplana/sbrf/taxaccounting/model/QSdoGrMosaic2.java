package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGrMosaic2 is a Querydsl query type for QSdoGrMosaic2
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGrMosaic2 extends com.querydsl.sql.RelationalPathBase<QSdoGrMosaic2> {

    private static final long serialVersionUID = 1849332860;

    public static final QSdoGrMosaic2 sdoGrMosaic2 = new QSdoGrMosaic2("SDO_GR_MOSAIC_2");

    public final NumberPath<java.math.BigInteger> b0 = createNumber("b0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> b1 = createNumber("b1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> c0 = createNumber("c0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> c1 = createNumber("c1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> csize = createNumber("csize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> r0 = createNumber("r0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> r1 = createNumber("r1", java.math.BigInteger.class);

    public final SimplePath<Object> rid = createSimple("rid", Object.class);

    public final NumberPath<java.math.BigInteger> rsize = createNumber("rsize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ultc = createNumber("ultc", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ultr = createNumber("ultr", java.math.BigInteger.class);

    public QSdoGrMosaic2(String variable) {
        super(QSdoGrMosaic2.class, forVariable(variable), "MDSYS", "SDO_GR_MOSAIC_2");
        addMetadata();
    }

    public QSdoGrMosaic2(String variable, String schema, String table) {
        super(QSdoGrMosaic2.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGrMosaic2(Path<? extends QSdoGrMosaic2> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GR_MOSAIC_2");
        addMetadata();
    }

    public QSdoGrMosaic2(PathMetadata metadata) {
        super(QSdoGrMosaic2.class, metadata, "MDSYS", "SDO_GR_MOSAIC_2");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(b0, ColumnMetadata.named("B0").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(b1, ColumnMetadata.named("B1").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c0, ColumnMetadata.named("C0").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c1, ColumnMetadata.named("C1").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(csize, ColumnMetadata.named("CSIZE").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(r0, ColumnMetadata.named("R0").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(r1, ColumnMetadata.named("R1").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rid, ColumnMetadata.named("RID").withIndex(1).ofType(Types.OTHER).withSize(10));
        addMetadata(rsize, ColumnMetadata.named("RSIZE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ultc, ColumnMetadata.named("ULTC").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ultr, ColumnMetadata.named("ULTR").withIndex(2).ofType(Types.DECIMAL).withSize(22));
    }

}

