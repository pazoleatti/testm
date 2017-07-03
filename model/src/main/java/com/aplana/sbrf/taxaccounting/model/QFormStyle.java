package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormStyle is a Querydsl query type for QFormStyle
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormStyle extends com.querydsl.sql.RelationalPathBase<QFormStyle> {

    private static final long serialVersionUID = -1858371888;

    public static final QFormStyle formStyle = new QFormStyle("FORM_STYLE");

    public final StringPath alias = createString("alias");

    public final NumberPath<Short> backColor = createNumber("backColor", Short.class);

    public final NumberPath<Byte> bold = createNumber("bold", Byte.class);

    public final NumberPath<Short> fontColor = createNumber("fontColor", Short.class);

    public final NumberPath<Integer> formTemplateId = createNumber("formTemplateId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Byte> italic = createNumber("italic", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QFormStyle> formStylePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QColor> formStyleFkBackColor = createForeignKey(backColor, "ID");

    public final com.querydsl.sql.ForeignKey<QFormTemplate> formStyleFkFormTemplateId = createForeignKey(formTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QColor> formStyleFkFontColor = createForeignKey(fontColor, "ID");

    public QFormStyle(String variable) {
        super(QFormStyle.class, forVariable(variable), "NDFL_1_0", "FORM_STYLE");
        addMetadata();
    }

    public QFormStyle(String variable, String schema, String table) {
        super(QFormStyle.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormStyle(Path<? extends QFormStyle> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_STYLE");
        addMetadata();
    }

    public QFormStyle(PathMetadata metadata) {
        super(QFormStyle.class, metadata, "NDFL_1_0", "FORM_STYLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(backColor, ColumnMetadata.named("BACK_COLOR").withIndex(5).ofType(Types.DECIMAL).withSize(3));
        addMetadata(bold, ColumnMetadata.named("BOLD").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(fontColor, ColumnMetadata.named("FONT_COLOR").withIndex(4).ofType(Types.DECIMAL).withSize(3));
        addMetadata(formTemplateId, ColumnMetadata.named("FORM_TEMPLATE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(italic, ColumnMetadata.named("ITALIC").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

