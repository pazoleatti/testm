package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormSearchResult is a Querydsl query type for QFormSearchResult
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormSearchResult extends com.querydsl.sql.RelationalPathBase<QFormSearchResult> {

    private static final long serialVersionUID = -96917018;

    public static final QFormSearchResult formSearchResult = new QFormSearchResult("FORM_SEARCH_RESULT");

    public final DateTimePath<org.joda.time.DateTime> date = createDateTime("date", org.joda.time.DateTime.class);

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath key = createString("key");

    public final NumberPath<Integer> rowsCount = createNumber("rowsCount", Integer.class);

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QFormSearchResult> formSearchResultPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormData> formSearchResultFkFormdata = createForeignKey(formDataId, "ID");

    public QFormSearchResult(String variable) {
        super(QFormSearchResult.class, forVariable(variable), "NDFL_1_0", "FORM_SEARCH_RESULT");
        addMetadata();
    }

    public QFormSearchResult(String variable, String schema, String table) {
        super(QFormSearchResult.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormSearchResult(Path<? extends QFormSearchResult> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_SEARCH_RESULT");
        addMetadata();
    }

    public QFormSearchResult(PathMetadata metadata) {
        super(QFormSearchResult.class, metadata, "NDFL_1_0", "FORM_SEARCH_RESULT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date, ColumnMetadata.named("DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(key, ColumnMetadata.named("KEY").withIndex(5).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(rowsCount, ColumnMetadata.named("ROWS_COUNT").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(sessionId, ColumnMetadata.named("SESSION_ID").withIndex(2).ofType(Types.DECIMAL).withSize(10));
    }

}

