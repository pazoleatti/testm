package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormSearchDataResultTmp is a Querydsl query type for QFormSearchDataResultTmp
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormSearchDataResultTmp extends com.querydsl.sql.RelationalPathBase<QFormSearchDataResultTmp> {

    private static final long serialVersionUID = -378365625;

    public static final QFormSearchDataResultTmp formSearchDataResultTmp = new QFormSearchDataResultTmp("FORM_SEARCH_DATA_RESULT_TMP");

    public final NumberPath<Integer> columnIndex = createNumber("columnIndex", Integer.class);

    public final StringPath rawValue = createString("rawValue");

    public final NumberPath<Integer> rowIndex = createNumber("rowIndex", Integer.class);

    public QFormSearchDataResultTmp(String variable) {
        super(QFormSearchDataResultTmp.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT_TMP");
        addMetadata();
    }

    public QFormSearchDataResultTmp(String variable, String schema, String table) {
        super(QFormSearchDataResultTmp.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormSearchDataResultTmp(Path<? extends QFormSearchDataResultTmp> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT_TMP");
        addMetadata();
    }

    public QFormSearchDataResultTmp(PathMetadata metadata) {
        super(QFormSearchDataResultTmp.class, metadata, "NDFL_UNSTABLE", "FORM_SEARCH_DATA_RESULT_TMP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnIndex, ColumnMetadata.named("COLUMN_INDEX").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(rawValue, ColumnMetadata.named("RAW_VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(rowIndex, ColumnMetadata.named("ROW_INDEX").withIndex(1).ofType(Types.DECIMAL).withSize(9));
    }

}

