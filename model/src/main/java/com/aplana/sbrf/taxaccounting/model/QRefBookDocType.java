package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookDocType is a Querydsl query type for QRefBookDocType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookDocType extends com.querydsl.sql.RelationalPathBase<QRefBookDocType> {

    private static final long serialVersionUID = 1053867315;

    public static final QRefBookDocType refBookDocType = new QRefBookDocType("REF_BOOK_DOC_TYPE");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Byte> priority = createNumber("priority", Byte.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookDocType> refBookDocTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookIdDoc> _refBookIdDocDocIdFk = createInvForeignKey(id, "DOC_ID");

    public QRefBookDocType(String variable) {
        super(QRefBookDocType.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_DOC_TYPE");
        addMetadata();
    }

    public QRefBookDocType(String variable, String schema, String table) {
        super(QRefBookDocType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookDocType(Path<? extends QRefBookDocType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_DOC_TYPE");
        addMetadata();
    }

    public QRefBookDocType(PathMetadata metadata) {
        super(QRefBookDocType.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_DOC_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(priority, ColumnMetadata.named("PRIORITY").withIndex(7).ofType(Types.DECIMAL).withSize(2));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

