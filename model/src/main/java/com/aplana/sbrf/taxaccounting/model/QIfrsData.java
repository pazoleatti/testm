package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QIfrsData is a Querydsl query type for QIfrsData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIfrsData extends com.querydsl.sql.RelationalPathBase<QIfrsData> {

    private static final long serialVersionUID = 2035182853;

    public static final QIfrsData ifrsData = new QIfrsData("IFRS_DATA");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Integer> reportPeriodId = createNumber("reportPeriodId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QIfrsData> ifrsDataPk = createPrimaryKey(reportPeriodId);

    public final com.querydsl.sql.ForeignKey<QReportPeriod> ifrsDataFkReportPeriod = createForeignKey(reportPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> ifrsDataFkBlobData = createForeignKey(blobDataId, "ID");

    public QIfrsData(String variable) {
        super(QIfrsData.class, forVariable(variable), "NDFL_1_0", "IFRS_DATA");
        addMetadata();
    }

    public QIfrsData(String variable, String schema, String table) {
        super(QIfrsData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIfrsData(Path<? extends QIfrsData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "IFRS_DATA");
        addMetadata();
    }

    public QIfrsData(PathMetadata metadata) {
        super(QIfrsData.class, metadata, "NDFL_1_0", "IFRS_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36));
        addMetadata(reportPeriodId, ColumnMetadata.named("REPORT_PERIOD_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

