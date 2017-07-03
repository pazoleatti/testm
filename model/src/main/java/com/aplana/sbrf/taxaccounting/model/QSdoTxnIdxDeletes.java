package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTxnIdxDeletes is a Querydsl query type for QSdoTxnIdxDeletes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTxnIdxDeletes extends com.querydsl.sql.RelationalPathBase<QSdoTxnIdxDeletes> {

    private static final long serialVersionUID = 767954996;

    public static final QSdoTxnIdxDeletes sdoTxnIdxDeletes = new QSdoTxnIdxDeletes("SDO_TXN_IDX_DELETES");

    public final NumberPath<java.math.BigInteger> end1 = createNumber("end1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> end2 = createNumber("end2", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> end3 = createNumber("end3", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> end4 = createNumber("end4", java.math.BigInteger.class);

    public final StringPath rid = createString("rid");

    public final StringPath sdoTxnIdxId = createString("sdoTxnIdxId");

    public final NumberPath<java.math.BigInteger> start1 = createNumber("start1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> start2 = createNumber("start2", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> start3 = createNumber("start3", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> start4 = createNumber("start4", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QSdoTxnIdxDeletes> sysC005525 = createPrimaryKey(rid, sdoTxnIdxId);

    public QSdoTxnIdxDeletes(String variable) {
        super(QSdoTxnIdxDeletes.class, forVariable(variable), "MDSYS", "SDO_TXN_IDX_DELETES");
        addMetadata();
    }

    public QSdoTxnIdxDeletes(String variable, String schema, String table) {
        super(QSdoTxnIdxDeletes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTxnIdxDeletes(Path<? extends QSdoTxnIdxDeletes> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TXN_IDX_DELETES");
        addMetadata();
    }

    public QSdoTxnIdxDeletes(PathMetadata metadata) {
        super(QSdoTxnIdxDeletes.class, metadata, "MDSYS", "SDO_TXN_IDX_DELETES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(end1, ColumnMetadata.named("END_1").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(end2, ColumnMetadata.named("END_2").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(end3, ColumnMetadata.named("END_3").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(end4, ColumnMetadata.named("END_4").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rid, ColumnMetadata.named("RID").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(sdoTxnIdxId, ColumnMetadata.named("SDO_TXN_IDX_ID").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(start1, ColumnMetadata.named("START_1").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(start2, ColumnMetadata.named("START_2").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(start3, ColumnMetadata.named("START_3").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(start4, ColumnMetadata.named("START_4").withIndex(9).ofType(Types.DECIMAL).withSize(22));
    }

}

