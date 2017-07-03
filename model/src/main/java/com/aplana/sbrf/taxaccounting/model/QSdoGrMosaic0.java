package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGrMosaic0 is a Querydsl query type for QSdoGrMosaic0
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGrMosaic0 extends com.querydsl.sql.RelationalPathBase<QSdoGrMosaic0> {

    private static final long serialVersionUID = 1849332858;

    public static final QSdoGrMosaic0 sdoGrMosaic0 = new QSdoGrMosaic0("SDO_GR_MOSAIC_0");

    public final NumberPath<java.math.BigInteger> b0 = createNumber("b0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> b1 = createNumber("b1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> bands = createNumber("bands", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> bblksz = createNumber("bblksz", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> bcv = createNumber("bcv", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> c0 = createNumber("c0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> c1 = createNumber("c1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cblksz = createNumber("cblksz", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cdl = createNumber("cdl", java.math.BigInteger.class);

    public final StringPath cdp = createString("cdp");

    public final NumberPath<java.math.BigInteger> cols = createNumber("cols", java.math.BigInteger.class);

    public final StringPath cptype = createString("cptype");

    public final StringPath ilv = createString("ilv");

    public final SimplePath<Object> meta = createSimple("meta", Object.class);

    public final NumberPath<java.math.BigInteger> r0 = createNumber("r0", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> r1 = createNumber("r1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rblksz = createNumber("rblksz", java.math.BigInteger.class);

    public final StringPath rctif = createString("rctif");

    public final StringPath rdt = createString("rdt");

    public final SimplePath<Object> rid = createSimple("rid", Object.class);

    public final NumberPath<java.math.BigInteger> rrows = createNumber("rrows", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rstid = createNumber("rstid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rstype = createNumber("rstype", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> srid = createNumber("srid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ultb = createNumber("ultb", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ultc = createNumber("ultc", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ultr = createNumber("ultr", java.math.BigInteger.class);

    public QSdoGrMosaic0(String variable) {
        super(QSdoGrMosaic0.class, forVariable(variable), "MDSYS", "SDO_GR_MOSAIC_0");
        addMetadata();
    }

    public QSdoGrMosaic0(String variable, String schema, String table) {
        super(QSdoGrMosaic0.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGrMosaic0(Path<? extends QSdoGrMosaic0> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GR_MOSAIC_0");
        addMetadata();
    }

    public QSdoGrMosaic0(PathMetadata metadata) {
        super(QSdoGrMosaic0.class, metadata, "MDSYS", "SDO_GR_MOSAIC_0");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(b0, ColumnMetadata.named("B0").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(b1, ColumnMetadata.named("B1").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(bands, ColumnMetadata.named("BANDS").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(bblksz, ColumnMetadata.named("BBLKSZ").withIndex(18).ofType(Types.DECIMAL).withSize(22));
        addMetadata(bcv, ColumnMetadata.named("BCV").withIndex(14).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c0, ColumnMetadata.named("C0").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c1, ColumnMetadata.named("C1").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cblksz, ColumnMetadata.named("CBLKSZ").withIndex(17).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cdl, ColumnMetadata.named("CDL").withIndex(19).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cdp, ColumnMetadata.named("CDP").withIndex(20).ofType(Types.VARCHAR).withSize(50));
        addMetadata(cols, ColumnMetadata.named("COLS").withIndex(25).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cptype, ColumnMetadata.named("CPTYPE").withIndex(26).ofType(Types.VARCHAR).withSize(10));
        addMetadata(ilv, ColumnMetadata.named("ILV").withIndex(15).ofType(Types.VARCHAR).withSize(5));
        addMetadata(meta, ColumnMetadata.named("META").withIndex(27).ofType(2007).withSize(2000));
        addMetadata(r0, ColumnMetadata.named("R0").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(r1, ColumnMetadata.named("R1").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rblksz, ColumnMetadata.named("RBLKSZ").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rctif, ColumnMetadata.named("RCTIF").withIndex(6).ofType(Types.VARCHAR).withSize(10));
        addMetadata(rdt, ColumnMetadata.named("RDT").withIndex(2).ofType(Types.VARCHAR).withSize(100));
        addMetadata(rid, ColumnMetadata.named("RID").withIndex(1).ofType(Types.OTHER).withSize(10));
        addMetadata(rrows, ColumnMetadata.named("RROWS").withIndex(24).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rstid, ColumnMetadata.named("RSTID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rstype, ColumnMetadata.named("RSTYPE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ultb, ColumnMetadata.named("ULTB").withIndex(23).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ultc, ColumnMetadata.named("ULTC").withIndex(22).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ultr, ColumnMetadata.named("ULTR").withIndex(21).ofType(Types.DECIMAL).withSize(22));
    }

}

