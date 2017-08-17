package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QGetRecordsTest is a Querydsl query type for QGetRecordsTest
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QGetRecordsTest extends com.querydsl.sql.RelationalPathBase<QGetRecordsTest> {

    private static final long serialVersionUID = -717823205;

    public static final QGetRecordsTest getRecordsTest = new QGetRecordsTest("GET_RECORDS_TEST");

    public final StringPath a = createString("a");

    public final StringPath b = createString("b");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public QGetRecordsTest(String variable) {
        super(QGetRecordsTest.class, forVariable(variable), "NDFL_UNSTABLE", "GET_RECORDS_TEST");
        addMetadata();
    }

    public QGetRecordsTest(String variable, String schema, String table) {
        super(QGetRecordsTest.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QGetRecordsTest(Path<? extends QGetRecordsTest> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "GET_RECORDS_TEST");
        addMetadata();
    }

    public QGetRecordsTest(PathMetadata metadata) {
        super(QGetRecordsTest.class, metadata, "NDFL_UNSTABLE", "GET_RECORDS_TEST");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(a, ColumnMetadata.named("A").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(b, ColumnMetadata.named("B").withIndex(6).ofType(Types.VARCHAR).withSize(100));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

