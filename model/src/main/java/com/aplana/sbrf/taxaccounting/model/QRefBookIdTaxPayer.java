package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookIdTaxPayer is a Querydsl query type for QRefBookIdTaxPayer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookIdTaxPayer extends com.querydsl.sql.RelationalPathBase<QRefBookIdTaxPayer> {

    private static final long serialVersionUID = -508076956;

    public static final QRefBookIdTaxPayer refBookIdTaxPayer = new QRefBookIdTaxPayer("REF_BOOK_ID_TAX_PAYER");

    public final NumberPath<Long> asNu = createNumber("asNu", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inp = createString("inp");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookIdTaxPayer> refBookIdTaxPayerPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> refBookIdTaxPayerAsNuFk = createForeignKey(asNu, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> refBookIdTaxPayerPersFk = createForeignKey(personId, "ID");

    public QRefBookIdTaxPayer(String variable) {
        super(QRefBookIdTaxPayer.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ID_TAX_PAYER");
        addMetadata();
    }

    public QRefBookIdTaxPayer(String variable, String schema, String table) {
        super(QRefBookIdTaxPayer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookIdTaxPayer(Path<? extends QRefBookIdTaxPayer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ID_TAX_PAYER");
        addMetadata();
    }

    public QRefBookIdTaxPayer(PathMetadata metadata) {
        super(QRefBookIdTaxPayer.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ID_TAX_PAYER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(asNu, ColumnMetadata.named("AS_NU").withIndex(4).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(inp, ColumnMetadata.named("INP").withIndex(3).ofType(Types.VARCHAR).withSize(25).notNull());
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(6).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

