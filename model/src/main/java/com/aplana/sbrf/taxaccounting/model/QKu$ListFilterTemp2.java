package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QKu$ListFilterTemp2 is a Querydsl query type for QKu$ListFilterTemp2
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QKu$ListFilterTemp2 extends com.querydsl.sql.RelationalPathBase<QKu$ListFilterTemp2> {

    private static final long serialVersionUID = -34335925;

    public static final QKu$ListFilterTemp2 ku$ListFilterTemp2 = new QKu$ListFilterTemp2("KU$_LIST_FILTER_TEMP_2");

    public final NumberPath<java.math.BigInteger> baseProcessOrder = createNumber("baseProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> duplicate = createNumber("duplicate", java.math.BigInteger.class);

    public final StringPath objectName = createString("objectName");

    public final StringPath objectSchema = createString("objectSchema");

    public final NumberPath<java.math.BigInteger> parentProcessOrder = createNumber("parentProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> processOrder = createNumber("processOrder", java.math.BigInteger.class);

    public QKu$ListFilterTemp2(String variable) {
        super(QKu$ListFilterTemp2.class, forVariable(variable), "SYS", "KU$_LIST_FILTER_TEMP_2");
        addMetadata();
    }

    public QKu$ListFilterTemp2(String variable, String schema, String table) {
        super(QKu$ListFilterTemp2.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QKu$ListFilterTemp2(Path<? extends QKu$ListFilterTemp2> path) {
        super(path.getType(), path.getMetadata(), "SYS", "KU$_LIST_FILTER_TEMP_2");
        addMetadata();
    }

    public QKu$ListFilterTemp2(PathMetadata metadata) {
        super(QKu$ListFilterTemp2.class, metadata, "SYS", "KU$_LIST_FILTER_TEMP_2");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(baseProcessOrder, ColumnMetadata.named("BASE_PROCESS_ORDER").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(duplicate, ColumnMetadata.named("DUPLICATE").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectName, ColumnMetadata.named("OBJECT_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(500));
        addMetadata(objectSchema, ColumnMetadata.named("OBJECT_SCHEMA").withIndex(3).ofType(Types.VARCHAR).withSize(60));
        addMetadata(parentProcessOrder, ColumnMetadata.named("PARENT_PROCESS_ORDER").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(processOrder, ColumnMetadata.named("PROCESS_ORDER").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

