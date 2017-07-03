package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOl$nodes is a Querydsl query type for QOl$nodes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOl$nodes extends com.querydsl.sql.RelationalPathBase<QOl$nodes> {

    private static final long serialVersionUID = -1599210169;

    public static final QOl$nodes ol$nodes = new QOl$nodes("OL$NODES");

    public final StringPath category = createString("category");

    public final NumberPath<java.math.BigInteger> nodeId = createNumber("nodeId", java.math.BigInteger.class);

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<java.math.BigInteger> nodeTextlen = createNumber("nodeTextlen", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> nodeTextoff = createNumber("nodeTextoff", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> nodeType = createNumber("nodeType", java.math.BigInteger.class);

    public final StringPath olName = createString("olName");

    public final NumberPath<java.math.BigInteger> parentId = createNumber("parentId", java.math.BigInteger.class);

    public QOl$nodes(String variable) {
        super(QOl$nodes.class, forVariable(variable), "SYSTEM", "OL$NODES");
        addMetadata();
    }

    public QOl$nodes(String variable, String schema, String table) {
        super(QOl$nodes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOl$nodes(Path<? extends QOl$nodes> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "OL$NODES");
        addMetadata();
    }

    public QOl$nodes(PathMetadata metadata) {
        super(QOl$nodes.class, metadata, "SYSTEM", "OL$NODES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(category, ColumnMetadata.named("CATEGORY").withIndex(2).ofType(Types.VARCHAR).withSize(30));
        addMetadata(nodeId, ColumnMetadata.named("NODE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(64));
        addMetadata(nodeTextlen, ColumnMetadata.named("NODE_TEXTLEN").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(nodeTextoff, ColumnMetadata.named("NODE_TEXTOFF").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(nodeType, ColumnMetadata.named("NODE_TYPE").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(olName, ColumnMetadata.named("OL_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(22));
    }

}

