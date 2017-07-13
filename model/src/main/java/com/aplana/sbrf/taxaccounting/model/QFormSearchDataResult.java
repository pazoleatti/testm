package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormSearchDataResult is a Querydsl query type for QFormSearchDataResult
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormSearchDataResult extends com.querydsl.sql.RelationalPathBase<QFormSearchDataResult> {

    private static final long serialVersionUID = -268889680;

    public static final QFormSearchDataResult formSearchDataResult = new QFormSearchDataResult("FORM_SEARCH_DATA_RESULT");

    public final NumberPath<Integer> columnIndex = createNumber("columnIndex", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final StringPath rawValue = createString("rawValue");

    public final NumberPath<Integer> rowIndex = createNumber("rowIndex", Integer.class);

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public QFormSearchDataResult(String variable) {
        super(QFormSearchDataResult.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT");
        addMetadata();
    }

    public QFormSearchDataResult(String variable, String schema, String table) {
        super(QFormSearchDataResult.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormSearchDataResult(Path<? extends QFormSearchDataResult> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT");
        addMetadata();
    }

    public QFormSearchDataResult(PathMetadata metadata) {
        super(QFormSearchDataResult.class, metadata, "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnIndex, ColumnMetadata.named("COLUMN_INDEX").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(rawValue, ColumnMetadata.named("RAW_VALUE").withIndex(5).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(rowIndex, ColumnMetadata.named("ROW_INDEX").withIndex(3).ofType(Types.DECIMAL).withSize(9));
        addMetadata(sessionId, ColumnMetadata.named("SESSION_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10));
    }

}

