package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationKind is a Querydsl query type for QDeclarationKind
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationKind extends com.querydsl.sql.RelationalPathBase<QDeclarationKind> {

    private static final long serialVersionUID = 1765272945;

    public static final QDeclarationKind declarationKind = new QDeclarationKind("DECLARATION_KIND");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QDeclarationKind> declarationKindPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> _declarationTemplateFkindFk = createInvForeignKey(id, "FORM_KIND");

    public QDeclarationKind(String variable) {
        super(QDeclarationKind.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_KIND");
        addMetadata();
    }

    public QDeclarationKind(String variable, String schema, String table) {
        super(QDeclarationKind.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationKind(Path<? extends QDeclarationKind> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_KIND");
        addMetadata();
    }

    public QDeclarationKind(PathMetadata metadata) {
        super(QDeclarationKind.class, metadata, "NDFL_UNSTABLE", "DECLARATION_KIND");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

