package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookFormType is a Querydsl query type for QRefBookFormType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookFormType extends com.querydsl.sql.RelationalPathBase<QRefBookFormType> {

    private static final long serialVersionUID = -2047766115;

    public static final QRefBookFormType refBookFormType = new QRefBookFormType("REF_BOOK_FORM_TYPE");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath taxKind = createString("taxKind");

    public final com.querydsl.sql.PrimaryKey<QRefBookFormType> refBookFormTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> _declarationTemplateFtypeFk = createInvForeignKey(id, "FORM_TYPE");

    public QRefBookFormType(String variable) {
        super(QRefBookFormType.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_FORM_TYPE");
        addMetadata();
    }

    public QRefBookFormType(String variable, String schema, String table) {
        super(QRefBookFormType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookFormType(Path<? extends QRefBookFormType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_FORM_TYPE");
        addMetadata();
    }

    public QRefBookFormType(PathMetadata metadata) {
        super(QRefBookFormType.class, metadata, "NDFL_1_0", "REF_BOOK_FORM_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(14).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(taxKind, ColumnMetadata.named("TAX_KIND").withIndex(4).ofType(Types.VARCHAR).withSize(1).notNull());
    }

}

