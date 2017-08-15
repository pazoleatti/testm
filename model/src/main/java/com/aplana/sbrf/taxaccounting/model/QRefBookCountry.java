package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookCountry is a Querydsl query type for QRefBookCountry
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookCountry extends com.querydsl.sql.RelationalPathBase<QRefBookCountry> {

    private static final long serialVersionUID = 183756855;

    public static final QRefBookCountry refBookCountry = new QRefBookCountry("REF_BOOK_COUNTRY");

    public final StringPath code = createString("code");

    public final StringPath code2 = createString("code2");

    public final StringPath code3 = createString("code3");

    public final StringPath fullname = createString("fullname");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookCountry> refBookCountryPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> _refBookPersonCitizenshipFk = createInvForeignKey(id, "CITIZENSHIP");

    public final com.querydsl.sql.ForeignKey<QRefBookAddress> _refBookAddressCountryFk = createInvForeignKey(id, "COUNTRY_ID");

    public QRefBookCountry(String variable) {
        super(QRefBookCountry.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_COUNTRY");
        addMetadata();
    }

    public QRefBookCountry(String variable, String schema, String table) {
        super(QRefBookCountry.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookCountry(Path<? extends QRefBookCountry> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_COUNTRY");
        addMetadata();
    }

    public QRefBookCountry(PathMetadata metadata) {
        super(QRefBookCountry.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_COUNTRY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(code2, ColumnMetadata.named("CODE_2").withIndex(6).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(code3, ColumnMetadata.named("CODE_3").withIndex(7).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(fullname, ColumnMetadata.named("FULLNAME").withIndex(9).ofType(Types.VARCHAR).withSize(500));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(8).ofType(Types.VARCHAR).withSize(500).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

