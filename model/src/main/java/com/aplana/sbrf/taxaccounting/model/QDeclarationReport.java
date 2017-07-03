package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationReport is a Querydsl query type for QDeclarationReport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationReport extends com.querydsl.sql.RelationalPathBase<QDeclarationReport> {

    private static final long serialVersionUID = 112002001;

    public static final QDeclarationReport declarationReport = new QDeclarationReport("DECLARATION_REPORT");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final NumberPath<Integer> subreportId = createNumber("subreportId", Integer.class);

    public final NumberPath<Byte> type = createNumber("type", Byte.class);

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreport> declReportFkDeclSubreport = createForeignKey(subreportId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> declReportFkDeclData = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> declReportFkBlobData = createForeignKey(blobDataId, "ID");

    public QDeclarationReport(String variable) {
        super(QDeclarationReport.class, forVariable(variable), "NDFL_1_0", "DECLARATION_REPORT");
        addMetadata();
    }

    public QDeclarationReport(String variable, String schema, String table) {
        super(QDeclarationReport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationReport(Path<? extends QDeclarationReport> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "DECLARATION_REPORT");
        addMetadata();
    }

    public QDeclarationReport(PathMetadata metadata) {
        super(QDeclarationReport.class, metadata, "NDFL_1_0", "DECLARATION_REPORT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(subreportId, ColumnMetadata.named("SUBREPORT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

