package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookDetachTaxPay is a Querydsl query type for QRefBookDetachTaxPay
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookDetachTaxPay extends com.querydsl.sql.RelationalPathBase<QRefBookDetachTaxPay> {

    private static final long serialVersionUID = -1584379281;

    public static final QRefBookDetachTaxPay refBookDetachTaxPay = new QRefBookDetachTaxPay("REF_BOOK_DETACH_TAX_PAY");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookDetachTaxPay> refBookDetachTaxPayPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetObligFk = createInvForeignKey(id, "OBLIGATION");

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetObligFk = createInvForeignKey(id, "OBLIGATION");

    public QRefBookDetachTaxPay(String variable) {
        super(QRefBookDetachTaxPay.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_DETACH_TAX_PAY");
        addMetadata();
    }

    public QRefBookDetachTaxPay(String variable, String schema, String table) {
        super(QRefBookDetachTaxPay.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookDetachTaxPay(Path<? extends QRefBookDetachTaxPay> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_DETACH_TAX_PAY");
        addMetadata();
    }

    public QRefBookDetachTaxPay(PathMetadata metadata) {
        super(QRefBookDetachTaxPay.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_DETACH_TAX_PAY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

