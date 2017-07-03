package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QKu$ListFilterTemp is a Querydsl query type for QKu$ListFilterTemp
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QKu$ListFilterTemp extends com.querydsl.sql.RelationalPathBase<QKu$ListFilterTemp> {

    private static final long serialVersionUID = -970938937;

    public static final QKu$ListFilterTemp ku$ListFilterTemp = new QKu$ListFilterTemp("KU$_LIST_FILTER_TEMP");

    public final NumberPath<java.math.BigInteger> baseProcessOrder = createNumber("baseProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> duplicate = createNumber("duplicate", java.math.BigInteger.class);

    public final StringPath objectName = createString("objectName");

    public final NumberPath<java.math.BigInteger> parentProcessOrder = createNumber("parentProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> processOrder = createNumber("processOrder", java.math.BigInteger.class);

    public QKu$ListFilterTemp(String variable) {
        super(QKu$ListFilterTemp.class, forVariable(variable), "SYS", "KU$_LIST_FILTER_TEMP");
        addMetadata();
    }

    public QKu$ListFilterTemp(String variable, String schema, String table) {
        super(QKu$ListFilterTemp.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QKu$ListFilterTemp(Path<? extends QKu$ListFilterTemp> path) {
        super(path.getType(), path.getMetadata(), "SYS", "KU$_LIST_FILTER_TEMP");
        addMetadata();
    }

    public QKu$ListFilterTemp(PathMetadata metadata) {
        super(QKu$ListFilterTemp.class, metadata, "SYS", "KU$_LIST_FILTER_TEMP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(baseProcessOrder, ColumnMetadata.named("BASE_PROCESS_ORDER").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(duplicate, ColumnMetadata.named("DUPLICATE").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectName, ColumnMetadata.named("OBJECT_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(500));
        addMetadata(parentProcessOrder, ColumnMetadata.named("PARENT_PROCESS_ORDER").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(processOrder, ColumnMetadata.named("PROCESS_ORDER").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

