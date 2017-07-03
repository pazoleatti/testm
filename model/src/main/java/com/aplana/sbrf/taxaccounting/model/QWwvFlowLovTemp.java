package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWwvFlowLovTemp is a Querydsl query type for QWwvFlowLovTemp
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWwvFlowLovTemp extends com.querydsl.sql.RelationalPathBase<QWwvFlowLovTemp> {

    private static final long serialVersionUID = -436709472;

    public static final QWwvFlowLovTemp wwvFlowLovTemp = new QWwvFlowLovTemp("WWV_FLOW_LOV_TEMP");

    public final StringPath disp = createString("disp");

    public final NumberPath<java.math.BigInteger> insertOrder = createNumber("insertOrder", java.math.BigInteger.class);

    public final StringPath val = createString("val");

    public QWwvFlowLovTemp(String variable) {
        super(QWwvFlowLovTemp.class, forVariable(variable), "APEX_030200", "WWV_FLOW_LOV_TEMP");
        addMetadata();
    }

    public QWwvFlowLovTemp(String variable, String schema, String table) {
        super(QWwvFlowLovTemp.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWwvFlowLovTemp(Path<? extends QWwvFlowLovTemp> path) {
        super(path.getType(), path.getMetadata(), "APEX_030200", "WWV_FLOW_LOV_TEMP");
        addMetadata();
    }

    public QWwvFlowLovTemp(PathMetadata metadata) {
        super(QWwvFlowLovTemp.class, metadata, "APEX_030200", "WWV_FLOW_LOV_TEMP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(disp, ColumnMetadata.named("DISP").withIndex(2).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(insertOrder, ColumnMetadata.named("INSERT_ORDER").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(val, ColumnMetadata.named("VAL").withIndex(3).ofType(Types.VARCHAR).withSize(4000));
    }

}

