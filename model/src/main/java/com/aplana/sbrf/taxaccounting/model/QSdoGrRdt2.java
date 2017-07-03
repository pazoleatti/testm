package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGrRdt2 is a Querydsl query type for QSdoGrRdt2
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGrRdt2 extends com.querydsl.sql.RelationalPathBase<QSdoGrRdt2> {

    private static final long serialVersionUID = -1004375844;

    public static final QSdoGrRdt2 sdoGrRdt2 = new QSdoGrRdt2("SDO_GR_RDT_2");

    public final NumberPath<java.math.BigInteger> bandblocknumber = createNumber("bandblocknumber", java.math.BigInteger.class);

    public final SimplePath<Object> blockmbr = createSimple("blockmbr", Object.class);

    public final NumberPath<java.math.BigInteger> columnblocknumber = createNumber("columnblocknumber", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pyramidlevel = createNumber("pyramidlevel", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> rasterblock = createSimple("rasterblock", java.sql.Blob.class);

    public final NumberPath<java.math.BigInteger> rasterid = createNumber("rasterid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rowblocknumber = createNumber("rowblocknumber", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QSdoGrRdt2> sdoGrRdt2Pk = createPrimaryKey(bandblocknumber, columnblocknumber, pyramidlevel, rasterid, rowblocknumber);

    public QSdoGrRdt2(String variable) {
        super(QSdoGrRdt2.class, forVariable(variable), "MDSYS", "SDO_GR_RDT_2");
        addMetadata();
    }

    public QSdoGrRdt2(String variable, String schema, String table) {
        super(QSdoGrRdt2.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGrRdt2(Path<? extends QSdoGrRdt2> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GR_RDT_2");
        addMetadata();
    }

    public QSdoGrRdt2(PathMetadata metadata) {
        super(QSdoGrRdt2.class, metadata, "MDSYS", "SDO_GR_RDT_2");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bandblocknumber, ColumnMetadata.named("BANDBLOCKNUMBER").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(blockmbr, ColumnMetadata.named("BLOCKMBR").withIndex(6).ofType(Types.OTHER).withSize(1));
        addMetadata(columnblocknumber, ColumnMetadata.named("COLUMNBLOCKNUMBER").withIndex(5).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(pyramidlevel, ColumnMetadata.named("PYRAMIDLEVEL").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(rasterblock, ColumnMetadata.named("RASTERBLOCK").withIndex(7).ofType(Types.BLOB).withSize(4000));
        addMetadata(rasterid, ColumnMetadata.named("RASTERID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(rowblocknumber, ColumnMetadata.named("ROWBLOCKNUMBER").withIndex(4).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

