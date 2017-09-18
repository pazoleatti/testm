package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookIdDoc is a Querydsl query type for QRefBookIdDoc
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookIdDoc extends com.querydsl.sql.RelationalPathBase<QRefBookIdDoc> {

    private static final long serialVersionUID = 1162898174;

    public static final QRefBookIdDoc refBookIdDoc = new QRefBookIdDoc("REF_BOOK_ID_DOC");

    public final NumberPath<Long> docId = createNumber("docId", Long.class);

    public final StringPath docNumber = createString("docNumber");

    public final NumberPath<Long> duplicateRecordId = createNumber("duplicateRecordId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> incRep = createNumber("incRep", Byte.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookIdDoc> refBookIdDocPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookDocType> refBookIdDocDocIdFk = createForeignKey(docId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> refBookIdDocPersonFk = createForeignKey(personId, "ID");

    public QRefBookIdDoc(String variable) {
        super(QRefBookIdDoc.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ID_DOC");
        addMetadata();
    }

    public QRefBookIdDoc(String variable, String schema, String table) {
        super(QRefBookIdDoc.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookIdDoc(Path<? extends QRefBookIdDoc> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ID_DOC");
        addMetadata();
    }

    public QRefBookIdDoc(PathMetadata metadata) {
        super(QRefBookIdDoc.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ID_DOC");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(docId, ColumnMetadata.named("DOC_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18));
        addMetadata(docNumber, ColumnMetadata.named("DOC_NUMBER").withIndex(7).ofType(Types.VARCHAR).withSize(25));
        addMetadata(duplicateRecordId, ColumnMetadata.named("DUPLICATE_RECORD_ID").withIndex(9).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(incRep, ColumnMetadata.named("INC_REP").withIndex(8).ofType(Types.DECIMAL).withSize(1));
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

