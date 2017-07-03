package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTopoTransactData is a Querydsl query type for QSdoTopoTransactData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTopoTransactData extends com.querydsl.sql.RelationalPathBase<QSdoTopoTransactData> {

    private static final long serialVersionUID = -1881212241;

    public static final QSdoTopoTransactData sdoTopoTransactData = new QSdoTopoTransactData("SDO_TOPO_TRANSACT_DATA");

    public final NumberPath<java.math.BigInteger> parentId = createNumber("parentId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> topoId = createNumber("topoId", java.math.BigInteger.class);

    public final StringPath topologyId = createString("topologyId");

    public final StringPath topoOp = createString("topoOp");

    public final NumberPath<java.math.BigInteger> topoSequence = createNumber("topoSequence", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> topoType = createNumber("topoType", java.math.BigInteger.class);

    public QSdoTopoTransactData(String variable) {
        super(QSdoTopoTransactData.class, forVariable(variable), "MDSYS", "SDO_TOPO_TRANSACT_DATA");
        addMetadata();
    }

    public QSdoTopoTransactData(String variable, String schema, String table) {
        super(QSdoTopoTransactData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTopoTransactData(Path<? extends QSdoTopoTransactData> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TOPO_TRANSACT_DATA");
        addMetadata();
    }

    public QSdoTopoTransactData(PathMetadata metadata) {
        super(QSdoTopoTransactData.class, metadata, "MDSYS", "SDO_TOPO_TRANSACT_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topoId, ColumnMetadata.named("TOPO_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topologyId, ColumnMetadata.named("TOPOLOGY_ID").withIndex(2).ofType(Types.VARCHAR).withSize(20));
        addMetadata(topoOp, ColumnMetadata.named("TOPO_OP").withIndex(5).ofType(Types.VARCHAR).withSize(3));
        addMetadata(topoSequence, ColumnMetadata.named("TOPO_SEQUENCE").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topoType, ColumnMetadata.named("TOPO_TYPE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
    }

}

