package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTopoData$ is a Querydsl query type for QSdoTopoData$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTopoData$ extends com.querydsl.sql.RelationalPathBase<QSdoTopoData$> {

    private static final long serialVersionUID = -151739617;

    public static final QSdoTopoData$ sdoTopoData$ = new QSdoTopoData$("SDO_TOPO_DATA$");

    public final NumberPath<java.math.BigInteger> tgId = createNumber("tgId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> tgLayerId = createNumber("tgLayerId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> topoId = createNumber("topoId", java.math.BigInteger.class);

    public final StringPath topology = createString("topology");

    public final NumberPath<java.math.BigInteger> topoType = createNumber("topoType", java.math.BigInteger.class);

    public QSdoTopoData$(String variable) {
        super(QSdoTopoData$.class, forVariable(variable), "MDSYS", "SDO_TOPO_DATA$");
        addMetadata();
    }

    public QSdoTopoData$(String variable, String schema, String table) {
        super(QSdoTopoData$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTopoData$(Path<? extends QSdoTopoData$> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TOPO_DATA$");
        addMetadata();
    }

    public QSdoTopoData$(PathMetadata metadata) {
        super(QSdoTopoData$.class, metadata, "MDSYS", "SDO_TOPO_DATA$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(tgId, ColumnMetadata.named("TG_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tgLayerId, ColumnMetadata.named("TG_LAYER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topoId, ColumnMetadata.named("TOPO_ID").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topology, ColumnMetadata.named("TOPOLOGY").withIndex(1).ofType(Types.VARCHAR).withSize(20));
        addMetadata(topoType, ColumnMetadata.named("TOPO_TYPE").withIndex(5).ofType(Types.DECIMAL).withSize(22));
    }

}

