package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogClobQuery is a Querydsl query type for QLogClobQuery
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogClobQuery extends com.querydsl.sql.RelationalPathBase<QLogClobQuery> {

    private static final long serialVersionUID = 706557157;

    public static final QLogClobQuery logClobQuery = new QLogClobQuery("LOG_CLOB_QUERY");

    public final NumberPath<Integer> formTemplateId = createNumber("formTemplateId", Integer.class);

    public final NumberPath<Integer> formTypeId = createNumber("formTypeId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<org.joda.time.LocalDateTime> logDate = createDateTime("logDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public final StringPath sqlMode = createString("sqlMode");

    public final StringPath textQuery = createString("textQuery");

    public final com.querydsl.sql.PrimaryKey<QLogClobQuery> logClobQueryPk = createPrimaryKey(id);

    public QLogClobQuery(String variable) {
        super(QLogClobQuery.class, forVariable(variable), "NDFL_UNSTABLE", "LOG_CLOB_QUERY");
        addMetadata();
    }

    public QLogClobQuery(String variable, String schema, String table) {
        super(QLogClobQuery.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogClobQuery(Path<? extends QLogClobQuery> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOG_CLOB_QUERY");
        addMetadata();
    }

    public QLogClobQuery(PathMetadata metadata) {
        super(QLogClobQuery.class, metadata, "NDFL_UNSTABLE", "LOG_CLOB_QUERY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(formTemplateId, ColumnMetadata.named("FORM_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(formTypeId, ColumnMetadata.named("FORM_TYPE_ID").withIndex(7).ofType(Types.DECIMAL).withSize(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(logDate, ColumnMetadata.named("LOG_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(11).withDigits(6).notNull());
        addMetadata(sessionId, ColumnMetadata.named("SESSION_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sqlMode, ColumnMetadata.named("SQL_MODE").withIndex(3).ofType(Types.VARCHAR).withSize(10));
        addMetadata(textQuery, ColumnMetadata.named("TEXT_QUERY").withIndex(4).ofType(Types.CLOB).withSize(4000));
    }

}

