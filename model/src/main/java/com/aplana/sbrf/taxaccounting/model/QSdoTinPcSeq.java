package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTinPcSeq is a Querydsl query type for QSdoTinPcSeq
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTinPcSeq extends com.querydsl.sql.RelationalPathBase<QSdoTinPcSeq> {

    private static final long serialVersionUID = -565738124;

    public static final QSdoTinPcSeq sdoTinPcSeq = new QSdoTinPcSeq("SDO_TIN_PC_SEQ");

    public final NumberPath<java.math.BigInteger> curObjId = createNumber("curObjId", java.math.BigInteger.class);

    public final StringPath sdoOwner = createString("sdoOwner");

    public final StringPath tableName = createString("tableName");

    public final com.querydsl.sql.PrimaryKey<QSdoTinPcSeq> sdoPkSeqTinPc = createPrimaryKey(sdoOwner, tableName);

    public QSdoTinPcSeq(String variable) {
        super(QSdoTinPcSeq.class, forVariable(variable), "MDSYS", "SDO_TIN_PC_SEQ");
        addMetadata();
    }

    public QSdoTinPcSeq(String variable, String schema, String table) {
        super(QSdoTinPcSeq.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTinPcSeq(Path<? extends QSdoTinPcSeq> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TIN_PC_SEQ");
        addMetadata();
    }

    public QSdoTinPcSeq(PathMetadata metadata) {
        super(QSdoTinPcSeq.class, metadata, "MDSYS", "SDO_TIN_PC_SEQ");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(curObjId, ColumnMetadata.named("CUR_OBJ_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(sdoOwner, ColumnMetadata.named("SDO_OWNER").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(tableName, ColumnMetadata.named("TABLE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

