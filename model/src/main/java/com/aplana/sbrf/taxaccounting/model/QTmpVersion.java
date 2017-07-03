package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTmpVersion is a Querydsl query type for QTmpVersion
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTmpVersion extends com.querydsl.sql.RelationalPathBase<QTmpVersion> {

    private static final long serialVersionUID = 1473661086;

    public static final QTmpVersion tmpVersion = new QTmpVersion("TMP_VERSION");

    public final DateTimePath<org.joda.time.DateTime> calcDate = createDateTime("calcDate", org.joda.time.DateTime.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final DateTimePath<org.joda.time.DateTime> version = createDateTime("version", org.joda.time.DateTime.class);

    public QTmpVersion(String variable) {
        super(QTmpVersion.class, forVariable(variable), "NDFL_1_0", "TMP_VERSION");
        addMetadata();
    }

    public QTmpVersion(String variable, String schema, String table) {
        super(QTmpVersion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTmpVersion(Path<? extends QTmpVersion> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "TMP_VERSION");
        addMetadata();
    }

    public QTmpVersion(PathMetadata metadata) {
        super(QTmpVersion.class, metadata, "NDFL_1_0", "TMP_VERSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(calcDate, ColumnMetadata.named("CALC_DATE").withIndex(3).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(1).ofType(Types.TIMESTAMP).withSize(7));
    }

}

