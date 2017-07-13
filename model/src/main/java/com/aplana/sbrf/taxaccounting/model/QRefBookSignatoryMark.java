package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookSignatoryMark is a Querydsl query type for QRefBookSignatoryMark
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookSignatoryMark extends com.querydsl.sql.RelationalPathBase<QRefBookSignatoryMark> {

    private static final long serialVersionUID = -395887084;

    public static final QRefBookSignatoryMark refBookSignatoryMark = new QRefBookSignatoryMark("REF_BOOK_SIGNATORY_MARK");

    public final NumberPath<Byte> code = createNumber("code", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookSignatoryMark> refBookSignatoryMarkPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetSignatoryFk = createInvForeignKey(id, "SIGNATORY_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetSignatoryFk = createInvForeignKey(id, "SIGNATORY_ID");

    public QRefBookSignatoryMark(String variable) {
        super(QRefBookSignatoryMark.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_SIGNATORY_MARK");
        addMetadata();
    }

    public QRefBookSignatoryMark(String variable, String schema, String table) {
        super(QRefBookSignatoryMark.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookSignatoryMark(Path<? extends QRefBookSignatoryMark> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_SIGNATORY_MARK");
        addMetadata();
    }

    public QRefBookSignatoryMark(PathMetadata metadata) {
        super(QRefBookSignatoryMark.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_SIGNATORY_MARK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

