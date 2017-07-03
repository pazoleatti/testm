package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTinBlkTable is a Querydsl query type for QSdoTinBlkTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTinBlkTable extends com.querydsl.sql.RelationalPathBase<QSdoTinBlkTable> {

    private static final long serialVersionUID = 986295781;

    public static final QSdoTinBlkTable sdoTinBlkTable = new QSdoTinBlkTable("SDO_TIN_BLK_TABLE");

    public final SimplePath<Object> blkDomain = createSimple("blkDomain", Object.class);

    public final SimplePath<Object> blkExtent = createSimple("blkExtent", Object.class);

    public final NumberPath<java.math.BigInteger> blkId = createNumber("blkId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numPoints = createNumber("numPoints", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numTriangles = createNumber("numTriangles", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numUnsortedPoints = createNumber("numUnsortedPoints", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> objId = createNumber("objId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pcblkMaxRes = createNumber("pcblkMaxRes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> pcblkMinRes = createNumber("pcblkMinRes", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> points = createSimple("points", java.sql.Blob.class);

    public final NumberPath<java.math.BigInteger> ptSortDim = createNumber("ptSortDim", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> triangles = createSimple("triangles", java.sql.Blob.class);

    public final NumberPath<java.math.BigInteger> trLvl = createNumber("trLvl", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> trRes = createNumber("trRes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> trSortDim = createNumber("trSortDim", java.math.BigInteger.class);

    public QSdoTinBlkTable(String variable) {
        super(QSdoTinBlkTable.class, forVariable(variable), "MDSYS", "SDO_TIN_BLK_TABLE");
        addMetadata();
    }

    public QSdoTinBlkTable(String variable, String schema, String table) {
        super(QSdoTinBlkTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTinBlkTable(Path<? extends QSdoTinBlkTable> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TIN_BLK_TABLE");
        addMetadata();
    }

    public QSdoTinBlkTable(PathMetadata metadata) {
        super(QSdoTinBlkTable.class, metadata, "MDSYS", "SDO_TIN_BLK_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blkDomain, ColumnMetadata.named("BLK_DOMAIN").withIndex(4).ofType(Types.OTHER).withSize(1));
        addMetadata(blkExtent, ColumnMetadata.named("BLK_EXTENT").withIndex(3).ofType(Types.OTHER).withSize(1));
        addMetadata(blkId, ColumnMetadata.named("BLK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numPoints, ColumnMetadata.named("NUM_POINTS").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numTriangles, ColumnMetadata.named("NUM_TRIANGLES").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numUnsortedPoints, ColumnMetadata.named("NUM_UNSORTED_POINTS").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objId, ColumnMetadata.named("OBJ_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(pcblkMaxRes, ColumnMetadata.named("PCBLK_MAX_RES").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(pcblkMinRes, ColumnMetadata.named("PCBLK_MIN_RES").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(points, ColumnMetadata.named("POINTS").withIndex(10).ofType(Types.BLOB).withSize(4000));
        addMetadata(ptSortDim, ColumnMetadata.named("PT_SORT_DIM").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(triangles, ColumnMetadata.named("TRIANGLES").withIndex(15).ofType(Types.BLOB).withSize(4000));
        addMetadata(trLvl, ColumnMetadata.named("TR_LVL").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(trRes, ColumnMetadata.named("TR_RES").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(trSortDim, ColumnMetadata.named("TR_SORT_DIM").withIndex(14).ofType(Types.DECIMAL).withSize(22));
    }

}

