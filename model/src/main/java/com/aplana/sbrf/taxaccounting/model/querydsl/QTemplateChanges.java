package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTemplateChanges is a Querydsl query type for QTemplateChanges
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTemplateChanges extends com.querydsl.sql.RelationalPathBase<QTemplateChanges> {

    private static final long serialVersionUID = 222066141;

    public static final QTemplateChanges templateChanges = new QTemplateChanges("TEMPLATE_CHANGES");

    public final NumberPath<Integer> author = createNumber("author", Integer.class);

    public final DateTimePath<org.joda.time.LocalDateTime> dateEvent = createDateTime("dateEvent", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> declarationTemplateId = createNumber("declarationTemplateId", Integer.class);

    public final NumberPath<Integer> event = createNumber("event", Integer.class);

    public final NumberPath<Integer> formTemplateId = createNumber("formTemplateId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> refBookId = createNumber("refBookId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QTemplateChanges> templateChangesPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> templateChangesFkDecT = createForeignKey(declarationTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QEvent> templateChangesFkEvent = createForeignKey(event, "ID");

    public final com.querydsl.sql.ForeignKey<QSecUser> templateChangesFkUserId = createForeignKey(author, "ID");

    public QTemplateChanges(String variable) {
        super(QTemplateChanges.class, forVariable(variable), "NDFL_UNSTABLE", "TEMPLATE_CHANGES");
        addMetadata();
    }

    public QTemplateChanges(String variable, String schema, String table) {
        super(QTemplateChanges.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTemplateChanges(String variable, String schema) {
        super(QTemplateChanges.class, forVariable(variable), schema, "TEMPLATE_CHANGES");
        addMetadata();
    }

    public QTemplateChanges(Path<? extends QTemplateChanges> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TEMPLATE_CHANGES");
        addMetadata();
    }

    public QTemplateChanges(PathMetadata metadata) {
        super(QTemplateChanges.class, metadata, "NDFL_UNSTABLE", "TEMPLATE_CHANGES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(author, ColumnMetadata.named("AUTHOR").withIndex(5).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(dateEvent, ColumnMetadata.named("DATE_EVENT").withIndex(6).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(declarationTemplateId, ColumnMetadata.named("DECLARATION_TEMPLATE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9));
        addMetadata(event, ColumnMetadata.named("EVENT").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(formTemplateId, ColumnMetadata.named("FORM_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(refBookId, ColumnMetadata.named("REF_BOOK_ID").withIndex(7).ofType(Types.DECIMAL).withSize(9));
    }

}

