package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationTemplate is a Querydsl query type for QDeclarationTemplate
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationTemplate extends com.querydsl.sql.RelationalPathBase<QDeclarationTemplate> {

    private static final long serialVersionUID = -634763305;

    public static final QDeclarationTemplate declarationTemplate = new QDeclarationTemplate("DECLARATION_TEMPLATE");

    public final StringPath createScript = createString("createScript");

    public final NumberPath<Integer> declarationTypeId = createNumber("declarationTypeId", Integer.class);

    public final NumberPath<Long> formKind = createNumber("formKind", Long.class);

    public final NumberPath<Long> formType = createNumber("formType", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath jrxml = createString("jrxml");

    public final StringPath name = createString("name");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final StringPath xsd = createString("xsd");

    public final com.querydsl.sql.PrimaryKey<QDeclarationTemplate> declarationTemplatePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QBlobData> declarationTemFkBlobData = createForeignKey(xsd, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> decTemFkBlobDataJrxml = createForeignKey(jrxml, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookFormType> declarationTemplateFtypeFk = createForeignKey(formType, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationType> declarationTemplateFkDtype = createForeignKey(declarationTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationKind> declarationTemplateFkindFk = createForeignKey(formKind, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplateFile> _declTemplFileTemplateFk = createInvForeignKey(id, "DECLARATION_TEMPLATE_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> _declarationDataFkDeclTId = createInvForeignKey(id, "DECLARATION_TEMPLATE_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreport> _declSubrepFkDeclTemplate = createInvForeignKey(id, "DECLARATION_TEMPLATE_ID");

    public final com.querydsl.sql.ForeignKey<QTemplateChanges> _templateChangesFkDecT = createInvForeignKey(id, "DECLARATION_TEMPLATE_ID");

    public QDeclarationTemplate(String variable) {
        super(QDeclarationTemplate.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_TEMPLATE");
        addMetadata();
    }

    public QDeclarationTemplate(String variable, String schema, String table) {
        super(QDeclarationTemplate.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationTemplate(Path<? extends QDeclarationTemplate> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_TEMPLATE");
        addMetadata();
    }

    public QDeclarationTemplate(PathMetadata metadata) {
        super(QDeclarationTemplate.class, metadata, "NDFL_UNSTABLE", "DECLARATION_TEMPLATE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createScript, ColumnMetadata.named("CREATE_SCRIPT").withIndex(5).ofType(Types.CLOB).withSize(4000));
        addMetadata(declarationTypeId, ColumnMetadata.named("DECLARATION_TYPE_ID").withIndex(7).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(formKind, ColumnMetadata.named("FORM_KIND").withIndex(9).ofType(Types.DECIMAL).withSize(18));
        addMetadata(formType, ColumnMetadata.named("FORM_TYPE").withIndex(10).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(jrxml, ColumnMetadata.named("JRXML").withIndex(6).ofType(Types.VARCHAR).withSize(36));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(4).ofType(Types.VARCHAR).withSize(512).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(xsd, ColumnMetadata.named("XSD").withIndex(8).ofType(Types.VARCHAR).withSize(36));
    }

}

