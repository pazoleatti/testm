package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationDataFile is a Querydsl query type for QDeclarationDataFile
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationDataFile extends com.querydsl.sql.RelationalPathBase<QDeclarationDataFile> {

    private static final long serialVersionUID = 123993812;

    public static final QDeclarationDataFile declarationDataFile = new QDeclarationDataFile("DECLARATION_DATA_FILE");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final NumberPath<Long> fileTypeId = createNumber("fileTypeId", Long.class);

    public final StringPath note = createString("note");

    public final StringPath userDepartmentName = createString("userDepartmentName");

    public final StringPath userName = createString("userName");

    public final com.querydsl.sql.PrimaryKey<QDeclarationDataFile> declDataFilePk = createPrimaryKey(blobDataId, declarationDataId);

    public final com.querydsl.sql.ForeignKey<QBlobData> declDataFileFkBlobData = createForeignKey(blobDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttachFileType> declDataFileTypeIdFk = createForeignKey(fileTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> declDataFileFkDeclData = createForeignKey(declarationDataId, "ID");

    public QDeclarationDataFile(String variable) {
        super(QDeclarationDataFile.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_DATA_FILE");
        addMetadata();
    }

    public QDeclarationDataFile(String variable, String schema, String table) {
        super(QDeclarationDataFile.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationDataFile(String variable, String schema) {
        super(QDeclarationDataFile.class, forVariable(variable), schema, "DECLARATION_DATA_FILE");
        addMetadata();
    }

    public QDeclarationDataFile(Path<? extends QDeclarationDataFile> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_DATA_FILE");
        addMetadata();
    }

    public QDeclarationDataFile(PathMetadata metadata) {
        super(QDeclarationDataFile.class, metadata, "NDFL_UNSTABLE", "DECLARATION_DATA_FILE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(fileTypeId, ColumnMetadata.named("FILE_TYPE_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(5).ofType(Types.VARCHAR).withSize(512));
        addMetadata(userDepartmentName, ColumnMetadata.named("USER_DEPARTMENT_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(4000).notNull());
        addMetadata(userName, ColumnMetadata.named("USER_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(512).notNull());
    }

}

