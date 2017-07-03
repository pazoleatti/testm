package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogSystemReport is a Querydsl query type for QLogSystemReport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogSystemReport extends com.querydsl.sql.RelationalPathBase<QLogSystemReport> {

    private static final long serialVersionUID = 2059098858;

    public static final QLogSystemReport logSystemReport = new QLogSystemReport("LOG_SYSTEM_REPORT");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Integer> secUserId = createNumber("secUserId", Integer.class);

    public final NumberPath<Byte> type = createNumber("type", Byte.class);

    public final com.querydsl.sql.ForeignKey<QBlobData> logSystemReportFkBlobData = createForeignKey(blobDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QSecUser> logSystemReportFkSecUser = createForeignKey(secUserId, "ID");

    public QLogSystemReport(String variable) {
        super(QLogSystemReport.class, forVariable(variable), "NDFL_1_0", "LOG_SYSTEM_REPORT");
        addMetadata();
    }

    public QLogSystemReport(String variable, String schema, String table) {
        super(QLogSystemReport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogSystemReport(Path<? extends QLogSystemReport> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "LOG_SYSTEM_REPORT");
        addMetadata();
    }

    public QLogSystemReport(PathMetadata metadata) {
        super(QLogSystemReport.class, metadata, "NDFL_1_0", "LOG_SYSTEM_REPORT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(1).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(secUserId, ColumnMetadata.named("SEC_USER_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

