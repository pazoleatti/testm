package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QBlobData is a Querydsl query type for QBlobData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBlobData extends com.querydsl.sql.RelationalPathBase<QBlobData> {

    private static final long serialVersionUID = -789118349;

    public static final QBlobData blobData = new QBlobData("BLOB_DATA");

    public final DateTimePath<org.joda.time.LocalDateTime> creationDate = createDateTime("creationDate", org.joda.time.LocalDateTime.class);

    public final SimplePath<java.io.InputStream> data = createSimple("data", java.io.InputStream.class);

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QBlobData> blobDataPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplateFile> _declTemplFileBlobFk = createInvForeignKey(id, "BLOB_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreport> _declSubrepFkBlobData = createInvForeignKey(id, "BLOB_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> _declarationTemFkBlobData = createInvForeignKey(id, "XSD");

    public final com.querydsl.sql.ForeignKey<QDeclarationDataFile> _declDataFileFkBlobData = createInvForeignKey(id, "BLOB_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> _decTemFkBlobDataJrxml = createInvForeignKey(id, "JRXML");

    public final com.querydsl.sql.ForeignKey<QDeclarationReport> _declReportFkBlobData = createInvForeignKey(id, "BLOB_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkReportId = createInvForeignKey(id, "REPORT_ID");

    public final com.querydsl.sql.ForeignKey<QRefBook> _refBookFkScriptId = createInvForeignKey(id, "SCRIPT_ID");

    public QBlobData(String variable) {
        super(QBlobData.class, forVariable(variable), "NDFL_UNSTABLE", "BLOB_DATA");
        addMetadata();
    }

    public QBlobData(String variable, String schema, String table) {
        super(QBlobData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBlobData(String variable, String schema) {
        super(QBlobData.class, forVariable(variable), schema, "BLOB_DATA");
        addMetadata();
    }

    public QBlobData(Path<? extends QBlobData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "BLOB_DATA");
        addMetadata();
    }

    public QBlobData(PathMetadata metadata) {
        super(QBlobData.class, metadata, "NDFL_UNSTABLE", "BLOB_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationDate, ColumnMetadata.named("CREATION_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(data, ColumnMetadata.named("DATA").withIndex(3).ofType(Types.BLOB).withSize(4000).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(530));
    }

}

