package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataReport is a Querydsl query type for QFormDataReport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataReport extends com.querydsl.sql.RelationalPathBase<QFormDataReport> {

    private static final long serialVersionUID = -1478573121;

    public static final QFormDataReport formDataReport = new QFormDataReport("FORM_DATA_REPORT");

    public final NumberPath<Byte> absolute = createNumber("absolute", Byte.class);

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Byte> checking = createNumber("checking", Byte.class);

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final NumberPath<Byte> manual = createNumber("manual", Byte.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<QFormDataReport> formDataRepPk = createPrimaryKey(absolute, checking, formDataId, manual, type);

    public final com.querydsl.sql.ForeignKey<QFormData> formDataRepFkFormDataId = createForeignKey(formDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> formDataRepFkBlobDataId = createForeignKey(blobDataId, "ID");

    public QFormDataReport(String variable) {
        super(QFormDataReport.class, forVariable(variable), "NDFL_1_0", "FORM_DATA_REPORT");
        addMetadata();
    }

    public QFormDataReport(String variable, String schema, String table) {
        super(QFormDataReport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataReport(Path<? extends QFormDataReport> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_DATA_REPORT");
        addMetadata();
    }

    public QFormDataReport(PathMetadata metadata) {
        super(QFormDataReport.class, metadata, "NDFL_1_0", "FORM_DATA_REPORT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(absolute, ColumnMetadata.named("ABSOLUTE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(checking, ColumnMetadata.named("CHECKING").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(manual, ColumnMetadata.named("MANUAL").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(50).notNull());
    }

}

