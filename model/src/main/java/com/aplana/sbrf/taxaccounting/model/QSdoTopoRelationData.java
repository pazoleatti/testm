package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTopoRelationData is a Querydsl query type for QSdoTopoRelationData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTopoRelationData extends com.querydsl.sql.RelationalPathBase<QSdoTopoRelationData> {

    private static final long serialVersionUID = 197490369;

    public static final QSdoTopoRelationData sdoTopoRelationData = new QSdoTopoRelationData("SDO_TOPO_RELATION_DATA");

    public final NumberPath<java.math.BigInteger> tgId = createNumber("tgId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> tgLayerId = createNumber("tgLayerId", java.math.BigInteger.class);

    public final StringPath topoAttribute = createString("topoAttribute");

    public final NumberPath<java.math.BigInteger> topoId = createNumber("topoId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> topoType = createNumber("topoType", java.math.BigInteger.class);

    public QSdoTopoRelationData(String variable) {
        super(QSdoTopoRelationData.class, forVariable(variable), "MDSYS", "SDO_TOPO_RELATION_DATA");
        addMetadata();
    }

    public QSdoTopoRelationData(String variable, String schema, String table) {
        super(QSdoTopoRelationData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTopoRelationData(Path<? extends QSdoTopoRelationData> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TOPO_RELATION_DATA");
        addMetadata();
    }

    public QSdoTopoRelationData(PathMetadata metadata) {
        super(QSdoTopoRelationData.class, metadata, "MDSYS", "SDO_TOPO_RELATION_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(tgId, ColumnMetadata.named("TG_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tgLayerId, ColumnMetadata.named("TG_LAYER_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topoAttribute, ColumnMetadata.named("TOPO_ATTRIBUTE").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(topoId, ColumnMetadata.named("TOPO_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(topoType, ColumnMetadata.named("TOPO_TYPE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
    }

}

