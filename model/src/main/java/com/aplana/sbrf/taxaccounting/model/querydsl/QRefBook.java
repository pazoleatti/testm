package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBook is a Querydsl query type for QRefBook
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBook extends com.querydsl.sql.RelationalPathBase<QRefBook> {

    private static final long serialVersionUID = 802979600;

    public static final QRefBook refBook = new QRefBook("REF_BOOK");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> isVersioned = createNumber("isVersioned", Byte.class);

    public final StringPath name = createString("name");

    public final NumberPath<Byte> readOnly = createNumber("readOnly", Byte.class);

    public final NumberPath<Long> regionAttributeId = createNumber("regionAttributeId", Long.class);

    public final StringPath scriptId = createString("scriptId");

    public final StringPath tableName = createString("tableName");

    public final NumberPath<Byte> type = createNumber("type", Byte.class);

    public final NumberPath<Byte> visible = createNumber("visible", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QRefBook> refBookPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> refBookFkRegion = createForeignKey(regionAttributeId, "ID");

    public final com.querydsl.sql.ForeignKey<QBlobData> refBookFkScriptId = createForeignKey(scriptId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> _refBookAttrFkReferenceId = createInvForeignKey(id, "REFERENCE_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> _refBookAttrFkRefBookId = createInvForeignKey(id, "REF_BOOK_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookRecord> _refBookRecordFkRefBookId = createInvForeignKey(id, "REF_BOOK_ID");

    public QRefBook(String variable) {
        super(QRefBook.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK");
        addMetadata();
    }

    public QRefBook(String variable, String schema, String table) {
        super(QRefBook.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBook(String variable, String schema) {
        super(QRefBook.class, forVariable(variable), schema, "REF_BOOK");
        addMetadata();
    }

    public QRefBook(Path<? extends QRefBook> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK");
        addMetadata();
    }

    public QRefBook(PathMetadata metadata) {
        super(QRefBook.class, metadata, "NDFL_UNSTABLE", "REF_BOOK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(isVersioned, ColumnMetadata.named("IS_VERSIONED").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(readOnly, ColumnMetadata.named("READ_ONLY").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(regionAttributeId, ColumnMetadata.named("REGION_ATTRIBUTE_ID").withIndex(7).ofType(Types.DECIMAL).withSize(18));
        addMetadata(scriptId, ColumnMetadata.named("SCRIPT_ID").withIndex(3).ofType(Types.VARCHAR).withSize(36));
        addMetadata(tableName, ColumnMetadata.named("TABLE_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(100));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(visible, ColumnMetadata.named("VISIBLE").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

