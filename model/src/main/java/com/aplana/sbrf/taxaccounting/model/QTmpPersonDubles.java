package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTmpPersonDubles is a Querydsl query type for QTmpPersonDubles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTmpPersonDubles extends com.querydsl.sql.RelationalPathBase<QTmpPersonDubles> {

    private static final long serialVersionUID = -744571336;

    public static final QTmpPersonDubles tmpPersonDubles = new QTmpPersonDubles("TMP_PERSON_DUBLES");

    public final StringPath address = createString("address");

    public final NumberPath<Byte> addressType = createNumber("addressType", Byte.class);

    public final NumberPath<Long> addrId = createNumber("addrId", Long.class);

    public final StringPath appartment = createString("appartment");

    public final NumberPath<Long> asnuRefId = createNumber("asnuRefId", Long.class);

    public final DateTimePath<java.sql.Timestamp> birthDate = createDateTime("birthDate", java.sql.Timestamp.class);

    public final StringPath build = createString("build");

    public final NumberPath<Long> citizenshipRefId = createNumber("citizenshipRefId", Long.class);

    public final StringPath city = createString("city");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final StringPath district = createString("district");

    public final StringPath documentNumber = createString("documentNumber");

    public final NumberPath<Long> documentTypeRefId = createNumber("documentTypeRefId", Long.class);

    public final NumberPath<Byte> employee = createNumber("employee", Byte.class);

    public final StringPath firstName = createString("firstName");

    public final StringPath house = createString("house");

    public final StringPath inn = createString("inn");

    public final StringPath innForeign = createString("innForeign");

    public final StringPath inp = createString("inp");

    public final StringPath lastName = createString("lastName");

    public final StringPath locality = createString("locality");

    public final StringPath middleName = createString("middleName");

    public final NumberPath<Byte> midical = createNumber("midical", Byte.class);

    public final NumberPath<Byte> pension = createNumber("pension", Byte.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Long> personRecordId = createNumber("personRecordId", Long.class);

    public final NumberPath<Byte> persSt = createNumber("persSt", Byte.class);

    public final StringPath postalCode = createString("postalCode");

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final StringPath regionCode = createString("regionCode");

    public final NumberPath<Byte> sex = createNumber("sex", Byte.class);

    public final StringPath snils = createString("snils");

    public final NumberPath<Byte> social = createNumber("social", Byte.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final NumberPath<Long> statusRefId = createNumber("statusRefId", Long.class);

    public final StringPath street = createString("street");

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public QTmpPersonDubles(String variable) {
        super(QTmpPersonDubles.class, forVariable(variable), "NDFL_UNSTABLE", "TMP_PERSON_DUBLES");
        addMetadata();
    }

    public QTmpPersonDubles(String variable, String schema, String table) {
        super(QTmpPersonDubles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTmpPersonDubles(Path<? extends QTmpPersonDubles> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TMP_PERSON_DUBLES");
        addMetadata();
    }

    public QTmpPersonDubles(PathMetadata metadata) {
        super(QTmpPersonDubles.class, metadata, "NDFL_UNSTABLE", "TMP_PERSON_DUBLES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("ADDRESS").withIndex(36).ofType(Types.VARCHAR).withSize(255));
        addMetadata(addressType, ColumnMetadata.named("ADDRESS_TYPE").withIndex(23).ofType(Types.DECIMAL).withSize(1));
        addMetadata(addrId, ColumnMetadata.named("ADDR_ID").withIndex(22).ofType(Types.DECIMAL).withSize(18));
        addMetadata(appartment, ColumnMetadata.named("APPARTMENT").withIndex(33).ofType(Types.VARCHAR).withSize(20));
        addMetadata(asnuRefId, ColumnMetadata.named("ASNU_REF_ID").withIndex(21).ofType(Types.DECIMAL).withSize(18));
        addMetadata(birthDate, ColumnMetadata.named("BIRTH_DATE").withIndex(7).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(build, ColumnMetadata.named("BUILD").withIndex(32).ofType(Types.VARCHAR).withSize(20));
        addMetadata(citizenshipRefId, ColumnMetadata.named("CITIZENSHIP_REF_ID").withIndex(15).ofType(Types.DECIMAL).withSize(18));
        addMetadata(city, ColumnMetadata.named("CITY").withIndex(28).ofType(Types.VARCHAR).withSize(50));
        addMetadata(countryId, ColumnMetadata.named("COUNTRY_ID").withIndex(24).ofType(Types.DECIMAL).withSize(18));
        addMetadata(district, ColumnMetadata.named("DISTRICT").withIndex(27).ofType(Types.VARCHAR).withSize(50));
        addMetadata(documentNumber, ColumnMetadata.named("DOCUMENT_NUMBER").withIndex(18).ofType(Types.VARCHAR).withSize(25));
        addMetadata(documentTypeRefId, ColumnMetadata.named("DOCUMENT_TYPE_REF_ID").withIndex(19).ofType(Types.DECIMAL).withSize(18));
        addMetadata(employee, ColumnMetadata.named("EMPLOYEE").withIndex(14).ofType(Types.DECIMAL).withSize(1));
        addMetadata(firstName, ColumnMetadata.named("FIRST_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(60));
        addMetadata(house, ColumnMetadata.named("HOUSE").withIndex(31).ofType(Types.VARCHAR).withSize(20));
        addMetadata(inn, ColumnMetadata.named("INN").withIndex(8).ofType(Types.VARCHAR).withSize(12));
        addMetadata(innForeign, ColumnMetadata.named("INN_FOREIGN").withIndex(9).ofType(Types.VARCHAR).withSize(50));
        addMetadata(inp, ColumnMetadata.named("INP").withIndex(20).ofType(Types.VARCHAR).withSize(25));
        addMetadata(lastName, ColumnMetadata.named("LAST_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(60));
        addMetadata(locality, ColumnMetadata.named("LOCALITY").withIndex(29).ofType(Types.VARCHAR).withSize(50));
        addMetadata(middleName, ColumnMetadata.named("MIDDLE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(60));
        addMetadata(midical, ColumnMetadata.named("MIDICAL").withIndex(12).ofType(Types.DECIMAL).withSize(1));
        addMetadata(pension, ColumnMetadata.named("PENSION").withIndex(11).ofType(Types.DECIMAL).withSize(1));
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18));
        addMetadata(personRecordId, ColumnMetadata.named("PERSON_RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18));
        addMetadata(persSt, ColumnMetadata.named("PERS_ST").withIndex(37).ofType(Types.DECIMAL).withSize(1));
        addMetadata(postalCode, ColumnMetadata.named("POSTAL_CODE").withIndex(26).ofType(Types.VARCHAR).withSize(6));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(35).ofType(Types.DECIMAL).withSize(18));
        addMetadata(regionCode, ColumnMetadata.named("REGION_CODE").withIndex(25).ofType(Types.VARCHAR).withSize(2));
        addMetadata(sex, ColumnMetadata.named("SEX").withIndex(6).ofType(Types.DECIMAL).withSize(1));
        addMetadata(snils, ColumnMetadata.named("SNILS").withIndex(10).ofType(Types.VARCHAR).withSize(14));
        addMetadata(social, ColumnMetadata.named("SOCIAL").withIndex(13).ofType(Types.DECIMAL).withSize(1));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(34).ofType(Types.DECIMAL).withSize(1));
        addMetadata(statusRefId, ColumnMetadata.named("STATUS_REF_ID").withIndex(16).ofType(Types.DECIMAL).withSize(18));
        addMetadata(street, ColumnMetadata.named("STREET").withIndex(30).ofType(Types.VARCHAR).withSize(50));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(17).ofType(Types.TIMESTAMP).withSize(7));
    }

}

