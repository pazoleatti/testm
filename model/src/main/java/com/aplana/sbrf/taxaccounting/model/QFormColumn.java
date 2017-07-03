package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormColumn is a Querydsl query type for QFormColumn
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormColumn extends com.querydsl.sql.RelationalPathBase<QFormColumn> {

    private static final long serialVersionUID = 2056951319;

    public static final QFormColumn formColumn = new QFormColumn("FORM_COLUMN");

    public final StringPath alias = createString("alias");

    public final NumberPath<Long> attributeId = createNumber("attributeId", Long.class);

    public final NumberPath<Long> attributeId2 = createNumber("attributeId2", Long.class);

    public final NumberPath<Byte> checking = createNumber("checking", Byte.class);

    public final StringPath filter = createString("filter");

    public final NumberPath<Byte> format = createNumber("format", Byte.class);

    public final NumberPath<Integer> formTemplateId = createNumber("formTemplateId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Short> maxLength = createNumber("maxLength", Short.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> numerationRow = createNumber("numerationRow", Integer.class);

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final NumberPath<Integer> parentColumnId = createNumber("parentColumnId", Integer.class);

    public final NumberPath<Integer> precision = createNumber("precision", Integer.class);

    public final StringPath shortName = createString("shortName");

    public final StringPath type = createString("type");

    public final NumberPath<Integer> width = createNumber("width", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QFormColumn> formColumnPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> formColumnFkAttributeId2 = createForeignKey(attributeId2, "ID");

    public final com.querydsl.sql.ForeignKey<QFormColumn> formColumnFkParentId = createForeignKey(parentColumnId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> formColumnFkAttributeId = createForeignKey(attributeId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormTemplate> formColumnFkFormTemplId = createForeignKey(formTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormColumn> _formColumnFkParentId = createInvForeignKey(id, "PARENT_COLUMN_ID");

    public QFormColumn(String variable) {
        super(QFormColumn.class, forVariable(variable), "NDFL_1_0", "FORM_COLUMN");
        addMetadata();
    }

    public QFormColumn(String variable, String schema, String table) {
        super(QFormColumn.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormColumn(Path<? extends QFormColumn> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_COLUMN");
        addMetadata();
    }

    public QFormColumn(PathMetadata metadata) {
        super(QFormColumn.class, metadata, "NDFL_1_0", "FORM_COLUMN");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(5).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(11).ofType(Types.DECIMAL).withSize(18));
        addMetadata(attributeId2, ColumnMetadata.named("ATTRIBUTE_ID2").withIndex(15).ofType(Types.DECIMAL).withSize(18));
        addMetadata(checking, ColumnMetadata.named("CHECKING").withIndex(10).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(filter, ColumnMetadata.named("FILTER").withIndex(13).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(format, ColumnMetadata.named("FORMAT").withIndex(12).ofType(Types.DECIMAL).withSize(2));
        addMetadata(formTemplateId, ColumnMetadata.named("FORM_TEMPLATE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(maxLength, ColumnMetadata.named("MAX_LENGTH").withIndex(9).ofType(Types.DECIMAL).withSize(4));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(numerationRow, ColumnMetadata.named("NUMERATION_ROW").withIndex(16).ofType(Types.DECIMAL).withSize(9));
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(parentColumnId, ColumnMetadata.named("PARENT_COLUMN_ID").withIndex(14).ofType(Types.DECIMAL).withSize(9));
        addMetadata(precision, ColumnMetadata.named("PRECISION").withIndex(8).ofType(Types.DECIMAL).withSize(9));
        addMetadata(shortName, ColumnMetadata.named("SHORT_NAME").withIndex(17).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(width, ColumnMetadata.named("WIDTH").withIndex(7).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

