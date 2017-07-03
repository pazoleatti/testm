package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataFile is a Querydsl query type for QFormDataFile
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataFile extends com.querydsl.sql.RelationalPathBase<QFormDataFile> {

    private static final long serialVersionUID = -837645625;

    public static final QFormDataFile formDataFile = new QFormDataFile("FORM_DATA_FILE");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final StringPath note = createString("note");

    public final StringPath userDepartmentName = createString("userDepartmentName");

    public final StringPath userName = createString("userName");

    public final com.querydsl.sql.PrimaryKey<QFormDataFile> formDataFilePk = createPrimaryKey(blobDataId, formDataId);

    public final com.querydsl.sql.ForeignKey<QFormData> formDataFileFkFormData = createForeignKey(formDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> formDataFileFkBlobData = createForeignKey(blobDataId, "ID");

    public QFormDataFile(String variable) {
        super(QFormDataFile.class, forVariable(variable), "NDFL_1_0", "FORM_DATA_FILE");
        addMetadata();
    }

    public QFormDataFile(String variable, String schema, String table) {
        super(QFormDataFile.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataFile(Path<? extends QFormDataFile> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_DATA_FILE");
        addMetadata();
    }

    public QFormDataFile(PathMetadata metadata) {
        super(QFormDataFile.class, metadata, "NDFL_1_0", "FORM_DATA_FILE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(5).ofType(Types.VARCHAR).withSize(512));
        addMetadata(userDepartmentName, ColumnMetadata.named("USER_DEPARTMENT_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(4000).notNull());
        addMetadata(userName, ColumnMetadata.named("USER_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(512).notNull());
    }

}

