package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationTemplateFile is a Querydsl query type for QDeclarationTemplateFile
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationTemplateFile extends com.querydsl.sql.RelationalPathBase<QDeclarationTemplateFile> {

    private static final long serialVersionUID = 1200684196;

    public static final QDeclarationTemplateFile declarationTemplateFile = new QDeclarationTemplateFile("DECLARATION_TEMPLATE_FILE");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Long> declarationTemplateId = createNumber("declarationTemplateId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QDeclarationTemplateFile> declarationTemplateFilePk = createPrimaryKey(blobDataId, declarationTemplateId);

    public final com.querydsl.sql.ForeignKey<QBlobData> declTemplFileBlobFk = createForeignKey(blobDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> declTemplFileTemplateFk = createForeignKey(declarationTemplateId, "ID");

    public QDeclarationTemplateFile(String variable) {
        super(QDeclarationTemplateFile.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_TEMPLATE_FILE");
        addMetadata();
    }

    public QDeclarationTemplateFile(String variable, String schema, String table) {
        super(QDeclarationTemplateFile.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationTemplateFile(String variable, String schema) {
        super(QDeclarationTemplateFile.class, forVariable(variable), schema, "DECLARATION_TEMPLATE_FILE");
        addMetadata();
    }

    public QDeclarationTemplateFile(Path<? extends QDeclarationTemplateFile> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_TEMPLATE_FILE");
        addMetadata();
    }

    public QDeclarationTemplateFile(PathMetadata metadata) {
        super(QDeclarationTemplateFile.class, metadata, "NDFL_UNSTABLE", "DECLARATION_TEMPLATE_FILE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(2).ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(declarationTemplateId, ColumnMetadata.named("DECLARATION_TEMPLATE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

