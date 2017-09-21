package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclTemplateEventScript is a Querydsl query type for QDeclTemplateEventScript
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclTemplateEventScript extends com.querydsl.sql.RelationalPathBase<QDeclTemplateEventScript> {

    private static final long serialVersionUID = 1797474404;

    public static final QDeclTemplateEventScript declTemplateEventScript = new QDeclTemplateEventScript("DECL_TEMPLATE_EVENT_SCRIPT");

    public final NumberPath<java.math.BigInteger> declarationTemplateId = createNumber("declarationTemplateId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> eventId = createNumber("eventId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath script = createString("script");

    public final com.querydsl.sql.PrimaryKey<QDeclTemplateEventScript> sysC00622164 = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QEvent> decTempEventIdFk = createForeignKey(eventId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> decTempEventScrDecTempFk = createForeignKey(declarationTemplateId, "ID");

    public QDeclTemplateEventScript(String variable) {
        super(QDeclTemplateEventScript.class, forVariable(variable), "NDFL_UNSTABLE", "DECL_TEMPLATE_EVENT_SCRIPT");
        addMetadata();
    }

    public QDeclTemplateEventScript(String variable, String schema, String table) {
        super(QDeclTemplateEventScript.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclTemplateEventScript(Path<? extends QDeclTemplateEventScript> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECL_TEMPLATE_EVENT_SCRIPT");
        addMetadata();
    }

    public QDeclTemplateEventScript(PathMetadata metadata) {
        super(QDeclTemplateEventScript.class, metadata, "NDFL_UNSTABLE", "DECL_TEMPLATE_EVENT_SCRIPT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationTemplateId, ColumnMetadata.named("DECLARATION_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(eventId, ColumnMetadata.named("EVENT_ID").withIndex(3).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(script, ColumnMetadata.named("SCRIPT").withIndex(4).ofType(Types.CLOB).withSize(4000).notNull());
    }

}

