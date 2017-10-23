package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationSubreport is a Querydsl query type for QDeclarationSubreport
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationSubreport extends com.querydsl.sql.RelationalPathBase<QDeclarationSubreport> {

    private static final long serialVersionUID = 423635622;

    public static final QDeclarationSubreport declarationSubreport = new QDeclarationSubreport("DECLARATION_SUBREPORT");

    public final StringPath alias = createString("alias");

    public final StringPath blobDataId = createString("blobDataId");

    public final NumberPath<Integer> declarationTemplateId = createNumber("declarationTemplateId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final NumberPath<Byte> selectRecord = createNumber("selectRecord", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QDeclarationSubreport> declSubrepPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> declSubrepFkDeclTemplate = createForeignKey(declarationTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> declSubrepFkBlobData = createForeignKey(blobDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreportParams> _declSubrepParsSubrepIdFk = createInvForeignKey(id, "DECLARATION_SUBREPORT_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationReport> _declReportFkDeclSubreport = createInvForeignKey(id, "SUBREPORT_ID");

    public QDeclarationSubreport(String variable) {
        super(QDeclarationSubreport.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_SUBREPORT");
        addMetadata();
    }

    public QDeclarationSubreport(String variable, String schema, String table) {
        super(QDeclarationSubreport.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationSubreport(String variable, String schema) {
        super(QDeclarationSubreport.class, forVariable(variable), schema, "DECLARATION_SUBREPORT");
        addMetadata();
    }

    public QDeclarationSubreport(Path<? extends QDeclarationSubreport> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_SUBREPORT");
        addMetadata();
    }

    public QDeclarationSubreport(PathMetadata metadata) {
        super(QDeclarationSubreport.class, metadata, "NDFL_UNSTABLE", "DECLARATION_SUBREPORT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(blobDataId, ColumnMetadata.named("BLOB_DATA_ID").withIndex(6).ofType(Types.VARCHAR).withSize(36));
        addMetadata(declarationTemplateId, ColumnMetadata.named("DECLARATION_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(selectRecord, ColumnMetadata.named("SELECT_RECORD").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

