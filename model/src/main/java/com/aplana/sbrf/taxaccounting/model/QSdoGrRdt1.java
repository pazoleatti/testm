package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGrRdt1 is a Querydsl query type for QSdoGrRdt1
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGrRdt1 extends com.querydsl.sql.RelationalPathBase<QSdoGrRdt1> {

    private static final long serialVersionUID = -1004375845;

    public static final QSdoGrRdt1 sdoGrRdt1 = new QSdoGrRdt1("SDO_GR_RDT_1");

    public final NumberPath<java.math.BigInteger> bandblocknumber = createNumber("bandblocknumber", java.math.BigInteger.class);

    public final SimplePath<Object> blockmbr = createSimple("blockmbr", Object.class);

    public final NumberPath<java.math.BigInteger> columnblocknumber = createNumber("columnblocknumber", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pyramidlevel = createNumber("pyramidlevel", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> rasterblock = createSimple("rasterblock", java.sql.Blob.class);

    public final StringPath rasterdatatable = createString("rasterdatatable");

    public final NumberPath<java.math.BigInteger> rasterid = createNumber("rasterid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rowblocknumber = createNumber("rowblocknumber", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QSdoGrRdt1> sdoGrRdt1Pk = createPrimaryKey(bandblocknumber, columnblocknumber, pyramidlevel, rasterdatatable, rasterid, rowblocknumber);

    public QSdoGrRdt1(String variable) {
        super(QSdoGrRdt1.class, forVariable(variable), "MDSYS", "SDO_GR_RDT_1");
        addMetadata();
    }

    public QSdoGrRdt1(String variable, String schema, String table) {
        super(QSdoGrRdt1.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGrRdt1(Path<? extends QSdoGrRdt1> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GR_RDT_1");
        addMetadata();
    }

    public QSdoGrRdt1(PathMetadata metadata) {
        super(QSdoGrRdt1.class, metadata, "MDSYS", "SDO_GR_RDT_1");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bandblocknumber, ColumnMetadata.named("BANDBLOCKNUMBER").withIndex(4).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(blockmbr, ColumnMetadata.named("BLOCKMBR").withIndex(7).ofType(Types.OTHER).withSize(1));
        addMetadata(columnblocknumber, ColumnMetadata.named("COLUMNBLOCKNUMBER").withIndex(6).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(pyramidlevel, ColumnMetadata.named("PYRAMIDLEVEL").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(rasterblock, ColumnMetadata.named("RASTERBLOCK").withIndex(8).ofType(Types.BLOB).withSize(4000));
        addMetadata(rasterdatatable, ColumnMetadata.named("RASTERDATATABLE").withIndex(1).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(rasterid, ColumnMetadata.named("RASTERID").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(rowblocknumber, ColumnMetadata.named("ROWBLOCKNUMBER").withIndex(5).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

