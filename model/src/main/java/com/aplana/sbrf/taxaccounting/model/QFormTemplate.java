package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormTemplate is a Querydsl query type for QFormTemplate
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormTemplate extends com.querydsl.sql.RelationalPathBase<QFormTemplate> {

    private static final long serialVersionUID = 347136763;

    public static final QFormTemplate formTemplate = new QFormTemplate("FORM_TEMPLATE");

    public final NumberPath<Byte> accruing = createNumber("accruing", Byte.class);

    public final NumberPath<Byte> comparative = createNumber("comparative", Byte.class);

    public final StringPath dataHeaders = createString("dataHeaders");

    public final StringPath dataRows = createString("dataRows");

    public final NumberPath<Byte> fixedRows = createNumber("fixedRows", Byte.class);

    public final StringPath fullname = createString("fullname");

    public final StringPath header = createString("header");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Byte> monthly = createNumber("monthly", Byte.class);

    public final StringPath name = createString("name");

    public final StringPath script = createString("script");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    public final NumberPath<Byte> updating = createNumber("updating", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QFormTemplate> formTemplatePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormType> formTemplateFkTypeId = createForeignKey(typeId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormStyle> _formStyleFkFormTemplateId = createInvForeignKey(id, "FORM_TEMPLATE_ID");

    public final com.querydsl.sql.ForeignKey<QFormColumn> _formColumnFkFormTemplId = createInvForeignKey(id, "FORM_TEMPLATE_ID");

    public final com.querydsl.sql.ForeignKey<QFormData> _formDataFkFormTemplId = createInvForeignKey(id, "FORM_TEMPLATE_ID");

    public QFormTemplate(String variable) {
        super(QFormTemplate.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_TEMPLATE");
        addMetadata();
    }

    public QFormTemplate(String variable, String schema, String table) {
        super(QFormTemplate.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormTemplate(Path<? extends QFormTemplate> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_TEMPLATE");
        addMetadata();
    }

    public QFormTemplate(PathMetadata metadata) {
        super(QFormTemplate.class, metadata, "NDFL_UNSTABLE", "FORM_TEMPLATE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accruing, ColumnMetadata.named("ACCRUING").withIndex(14).ofType(Types.DECIMAL).withSize(1));
        addMetadata(comparative, ColumnMetadata.named("COMPARATIVE").withIndex(13).ofType(Types.DECIMAL).withSize(1));
        addMetadata(dataHeaders, ColumnMetadata.named("DATA_HEADERS").withIndex(8).ofType(Types.CLOB).withSize(4000));
        addMetadata(dataRows, ColumnMetadata.named("DATA_ROWS").withIndex(3).ofType(Types.CLOB).withSize(4000));
        addMetadata(fixedRows, ColumnMetadata.named("FIXED_ROWS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(fullname, ColumnMetadata.named("FULLNAME").withIndex(6).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(header, ColumnMetadata.named("HEADER").withIndex(12).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(monthly, ColumnMetadata.named("MONTHLY").withIndex(11).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(5).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(script, ColumnMetadata.named("SCRIPT").withIndex(7).ofType(Types.CLOB).withSize(4000));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(10).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(typeId, ColumnMetadata.named("TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(updating, ColumnMetadata.named("UPDATING").withIndex(15).ofType(Types.DECIMAL).withSize(1));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(9).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

