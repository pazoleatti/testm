package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWwvFlowDual100 is a Querydsl query type for QWwvFlowDual100
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWwvFlowDual100 extends com.querydsl.sql.RelationalPathBase<QWwvFlowDual100> {

    private static final long serialVersionUID = 1206239694;

    public static final QWwvFlowDual100 wwvFlowDual100 = new QWwvFlowDual100("WWV_FLOW_DUAL100");

    public final NumberPath<java.math.BigInteger> i = createNumber("i", java.math.BigInteger.class);

    public QWwvFlowDual100(String variable) {
        super(QWwvFlowDual100.class, forVariable(variable), "APEX_030200", "WWV_FLOW_DUAL100");
        addMetadata();
    }

    public QWwvFlowDual100(String variable, String schema, String table) {
        super(QWwvFlowDual100.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWwvFlowDual100(Path<? extends QWwvFlowDual100> path) {
        super(path.getType(), path.getMetadata(), "APEX_030200", "WWV_FLOW_DUAL100");
        addMetadata();
    }

    public QWwvFlowDual100(PathMetadata metadata) {
        super(QWwvFlowDual100.class, metadata, "APEX_030200", "WWV_FLOW_DUAL100");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(i, ColumnMetadata.named("I").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

