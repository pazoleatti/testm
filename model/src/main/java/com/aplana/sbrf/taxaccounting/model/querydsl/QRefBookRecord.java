package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookRecord is a Querydsl query type for QRefBookRecord
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookRecord extends com.querydsl.sql.RelationalPathBase<QRefBookRecord> {

    private static final long serialVersionUID = -229618815;

    public static final QRefBookRecord refBookRecord = new QRefBookRecord("REF_BOOK_RECORD");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Long> refBookId = createNumber("refBookId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookRecord> refBookRecordPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBook> refBookRecordFkRefBookId = createForeignKey(refBookId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookValue> _refBookValueFkRecordId = createInvForeignKey(id, "RECORD_ID");

    public QRefBookRecord(String variable) {
        super(QRefBookRecord.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_RECORD");
        addMetadata();
    }

    public QRefBookRecord(String variable, String schema, String table) {
        super(QRefBookRecord.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookRecord(String variable, String schema) {
        super(QRefBookRecord.class, forVariable(variable), schema, "REF_BOOK_RECORD");
        addMetadata();
    }

    public QRefBookRecord(Path<? extends QRefBookRecord> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_RECORD");
        addMetadata();
    }

    public QRefBookRecord(PathMetadata metadata) {
        super(QRefBookRecord.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_RECORD");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(refBookId, ColumnMetadata.named("REF_BOOK_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

