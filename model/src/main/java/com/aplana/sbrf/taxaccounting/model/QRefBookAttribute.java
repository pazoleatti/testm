package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookAttribute is a Querydsl query type for QRefBookAttribute
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookAttribute extends com.querydsl.sql.RelationalPathBase<QRefBookAttribute> {

    private static final long serialVersionUID = -842885379;

    public static final QRefBookAttribute refBookAttribute = new QRefBookAttribute("REF_BOOK_ATTRIBUTE");

    public final StringPath alias = createString("alias");

    public final NumberPath<Long> attributeId = createNumber("attributeId", Long.class);

    public final NumberPath<Integer> format = createNumber("format", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> isUnique = createNumber("isUnique", Byte.class);

    public final NumberPath<Short> maxLength = createNumber("maxLength", Short.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final NumberPath<Integer> precision = createNumber("precision", Integer.class);

    public final NumberPath<Byte> readOnly = createNumber("readOnly", Byte.class);

    public final NumberPath<Long> refBookId = createNumber("refBookId", Long.class);

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public final NumberPath<Byte> required = createNumber("required", Byte.class);

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final NumberPath<Byte> type = createNumber("type", Byte.class);

    public final NumberPath<Byte> visible = createNumber("visible", Byte.class);

    public final NumberPath<Integer> width = createNumber("width", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookAttribute> refBookAttrPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBook> refBookAttrFkReferenceId = createForeignKey(referenceId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBook> refBookAttrFkRefBookId = createForeignKey(refBookId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> refBookAttrFkAttributeId = createForeignKey(attributeId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormColumn> _formColumnFkAttributeId2 = createInvForeignKey(id, "ATTRIBUTE_ID2");

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreportParams> _declSubrepParsAttribIdFk = createInvForeignKey(id, "ATTRIBUTE_ID");

    public final com.querydsl.sql.ForeignKey<QFormColumn> _formColumnFkAttributeId = createInvForeignKey(id, "ATTRIBUTE_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookValue> _refBookValueFkAttributeId = createInvForeignKey(id, "ATTRIBUTE_ID");

    public final com.querydsl.sql.ForeignKey<QRefBook> _refBookFkRegion = createInvForeignKey(id, "REGION_ATTRIBUTE_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> _refBookAttrFkAttributeId = createInvForeignKey(id, "ATTRIBUTE_ID");

    public QRefBookAttribute(String variable) {
        super(QRefBookAttribute.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ATTRIBUTE");
        addMetadata();
    }

    public QRefBookAttribute(String variable, String schema, String table) {
        super(QRefBookAttribute.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookAttribute(Path<? extends QRefBookAttribute> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ATTRIBUTE");
        addMetadata();
    }

    public QRefBookAttribute(PathMetadata metadata) {
        super(QRefBookAttribute.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ATTRIBUTE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(4).ofType(Types.VARCHAR).withSize(30).notNull());
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(8).ofType(Types.DECIMAL).withSize(18));
        addMetadata(format, ColumnMetadata.named("FORMAT").withIndex(15).ofType(Types.DECIMAL).withSize(2));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(isUnique, ColumnMetadata.named("IS_UNIQUE").withIndex(13).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(maxLength, ColumnMetadata.named("MAX_LENGTH").withIndex(17).ofType(Types.DECIMAL).withSize(4));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(510).notNull());
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(6).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(precision, ColumnMetadata.named("PRECISION").withIndex(10).ofType(Types.DECIMAL).withSize(2));
        addMetadata(readOnly, ColumnMetadata.named("READ_ONLY").withIndex(16).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(refBookId, ColumnMetadata.named("REF_BOOK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(referenceId, ColumnMetadata.named("REFERENCE_ID").withIndex(7).ofType(Types.DECIMAL).withSize(18));
        addMetadata(required, ColumnMetadata.named("REQUIRED").withIndex(12).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(sortOrder, ColumnMetadata.named("SORT_ORDER").withIndex(14).ofType(Types.DECIMAL).withSize(9));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(visible, ColumnMetadata.named("VISIBLE").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(width, ColumnMetadata.named("WIDTH").withIndex(11).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

