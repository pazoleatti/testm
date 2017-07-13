package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookAddress is a Querydsl query type for QRefBookAddress
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookAddress extends com.querydsl.sql.RelationalPathBase<QRefBookAddress> {

    private static final long serialVersionUID = -1921766251;

    public static final QRefBookAddress refBookAddress = new QRefBookAddress("REF_BOOK_ADDRESS");

    public final StringPath address = createString("address");

    public final NumberPath<Byte> addressType = createNumber("addressType", Byte.class);

    public final StringPath appartment = createString("appartment");

    public final StringPath build = createString("build");

    public final StringPath city = createString("city");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final StringPath district = createString("district");

    public final StringPath house = createString("house");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath locality = createString("locality");

    public final StringPath postalCode = createString("postalCode");

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final StringPath regionCode = createString("regionCode");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final StringPath street = createString("street");

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookAddress> refBookAddressPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookCountry> refBookAddressCountryFk = createForeignKey(countryId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> _refBookPersonAddressFk = createInvForeignKey(id, "ADDRESS");

    public QRefBookAddress(String variable) {
        super(QRefBookAddress.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ADDRESS");
        addMetadata();
    }

    public QRefBookAddress(String variable, String schema, String table) {
        super(QRefBookAddress.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookAddress(Path<? extends QRefBookAddress> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ADDRESS");
        addMetadata();
    }

    public QRefBookAddress(PathMetadata metadata) {
        super(QRefBookAddress.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ADDRESS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("ADDRESS").withIndex(16).ofType(Types.VARCHAR).withSize(255));
        addMetadata(addressType, ColumnMetadata.named("ADDRESS_TYPE").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(appartment, ColumnMetadata.named("APPARTMENT").withIndex(12).ofType(Types.VARCHAR).withSize(20));
        addMetadata(build, ColumnMetadata.named("BUILD").withIndex(11).ofType(Types.VARCHAR).withSize(20));
        addMetadata(city, ColumnMetadata.named("CITY").withIndex(7).ofType(Types.VARCHAR).withSize(50));
        addMetadata(countryId, ColumnMetadata.named("COUNTRY_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(district, ColumnMetadata.named("DISTRICT").withIndex(6).ofType(Types.VARCHAR).withSize(50));
        addMetadata(house, ColumnMetadata.named("HOUSE").withIndex(10).ofType(Types.VARCHAR).withSize(20));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(locality, ColumnMetadata.named("LOCALITY").withIndex(8).ofType(Types.VARCHAR).withSize(50));
        addMetadata(postalCode, ColumnMetadata.named("POSTAL_CODE").withIndex(5).ofType(Types.VARCHAR).withSize(6));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(15).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(regionCode, ColumnMetadata.named("REGION_CODE").withIndex(4).ofType(Types.VARCHAR).withSize(2));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(14).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(street, ColumnMetadata.named("STREET").withIndex(9).ofType(Types.VARCHAR).withSize(50));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(13).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

