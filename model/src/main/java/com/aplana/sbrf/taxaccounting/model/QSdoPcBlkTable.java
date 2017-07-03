package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoPcBlkTable is a Querydsl query type for QSdoPcBlkTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoPcBlkTable extends com.querydsl.sql.RelationalPathBase<QSdoPcBlkTable> {

    private static final long serialVersionUID = 468270817;

    public static final QSdoPcBlkTable sdoPcBlkTable = new QSdoPcBlkTable("SDO_PC_BLK_TABLE");

    public final SimplePath<Object> blkDomain = createSimple("blkDomain", Object.class);

    public final SimplePath<Object> blkExtent = createSimple("blkExtent", Object.class);

    public final NumberPath<java.math.BigInteger> blkId = createNumber("blkId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numPoints = createNumber("numPoints", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numUnsortedPoints = createNumber("numUnsortedPoints", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> objId = createNumber("objId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pcblkMaxRes = createNumber("pcblkMaxRes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pcblkMinRes = createNumber("pcblkMinRes", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> points = createSimple("points", java.sql.Blob.class);

    public final NumberPath<java.math.BigInteger> ptSortDim = createNumber("ptSortDim", java.math.BigInteger.class);

    public QSdoPcBlkTable(String variable) {
        super(QSdoPcBlkTable.class, forVariable(variable), "MDSYS", "SDO_PC_BLK_TABLE");
        addMetadata();
    }

    public QSdoPcBlkTable(String variable, String schema, String table) {
        super(QSdoPcBlkTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoPcBlkTable(Path<? extends QSdoPcBlkTable> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_PC_BLK_TABLE");
        addMetadata();
    }

    public QSdoPcBlkTable(PathMetadata metadata) {
        super(QSdoPcBlkTable.class, metadata, "MDSYS", "SDO_PC_BLK_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blkDomain, ColumnMetadata.named("BLK_DOMAIN").withIndex(4).ofType(Types.OTHER).withSize(1));
        addMetadata(blkExtent, ColumnMetadata.named("BLK_EXTENT").withIndex(3).ofType(Types.OTHER).withSize(1));
        addMetadata(blkId, ColumnMetadata.named("BLK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numPoints, ColumnMetadata.named("NUM_POINTS").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numUnsortedPoints, ColumnMetadata.named("NUM_UNSORTED_POINTS").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objId, ColumnMetadata.named("OBJ_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(pcblkMaxRes, ColumnMetadata.named("PCBLK_MAX_RES").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(pcblkMinRes, ColumnMetadata.named("PCBLK_MIN_RES").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(points, ColumnMetadata.named("POINTS").withIndex(10).ofType(Types.BLOB).withSize(4000));
        addMetadata(ptSortDim, ColumnMetadata.named("PT_SORT_DIM").withIndex(9).ofType(Types.DECIMAL).withSize(22));
    }

}

