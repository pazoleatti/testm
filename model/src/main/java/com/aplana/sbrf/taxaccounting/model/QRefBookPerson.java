package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookPerson is a Querydsl query type for QRefBookPerson
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookPerson extends com.querydsl.sql.RelationalPathBase<QRefBookPerson> {

    private static final long serialVersionUID = 1892807316;

    public static final QRefBookPerson refBookPerson = new QRefBookPerson("REF_BOOK_PERSON");

    public final NumberPath<Long> address = createNumber("address", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> birthDate = createDateTime("birthDate", org.joda.time.LocalDateTime.class);

    public final StringPath birthPlace = createString("birthPlace");

    public final NumberPath<Long> citizenship = createNumber("citizenship", Long.class);

    public final NumberPath<Byte> employee = createNumber("employee", Byte.class);

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inn = createString("inn");

    public final StringPath innForeign = createString("innForeign");

    public final StringPath lastName = createString("lastName");

    public final NumberPath<Byte> medical = createNumber("medical", Byte.class);

    public final StringPath middleName = createString("middleName");

    public final NumberPath<Long> oldId = createNumber("oldId", Long.class);

    public final NumberPath<Byte> oldStatus = createNumber("oldStatus", Byte.class);

    public final NumberPath<Byte> pension = createNumber("pension", Byte.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> sex = createNumber("sex", Byte.class);

    public final StringPath snils = createString("snils");

    public final NumberPath<Byte> social = createNumber("social", Byte.class);

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final NumberPath<Long> taxpayerState = createNumber("taxpayerState", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookPerson> refBookPersonPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookCountry> refBookPersonCitizenshipFk = createForeignKey(citizenship, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookTaxpayerState> refBookPersonTaxpayerStFk = createForeignKey(taxpayerState, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> refBookPersonSourceFk = createForeignKey(sourceId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAddress> refBookPersonAddressFk = createForeignKey(address, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvPersSvStrahLic> _rsvPSvStrahLicPersonFk = createInvForeignKey(id, "PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QNdflReferences> _ndflRefersPersonFk = createInvForeignKey(id, "PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QNdflPerson> _ndflPersonFkPersonId = createInvForeignKey(id, "PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookIdDoc> _refBookIdDocPersonFk = createInvForeignKey(id, "PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookIdTaxPayer> _refBookIdTaxPayerPersFk = createInvForeignKey(id, "PERSON_ID");

    public QRefBookPerson(String variable) {
        super(QRefBookPerson.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_PERSON");
        addMetadata();
    }

    public QRefBookPerson(String variable, String schema, String table) {
        super(QRefBookPerson.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookPerson(Path<? extends QRefBookPerson> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_PERSON");
        addMetadata();
    }

    public QRefBookPerson(PathMetadata metadata) {
        super(QRefBookPerson.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_PERSON");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("ADDRESS").withIndex(13).ofType(Types.DECIMAL).withSize(18));
        addMetadata(birthDate, ColumnMetadata.named("BIRTH_DATE").withIndex(10).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(birthPlace, ColumnMetadata.named("BIRTH_PLACE").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(citizenship, ColumnMetadata.named("CITIZENSHIP").withIndex(12).ofType(Types.DECIMAL).withSize(18));
        addMetadata(employee, ColumnMetadata.named("EMPLOYEE").withIndex(17).ofType(Types.DECIMAL).withSize(1));
        addMetadata(firstName, ColumnMetadata.named("FIRST_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(60));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(inn, ColumnMetadata.named("INN").withIndex(6).ofType(Types.VARCHAR).withSize(12));
        addMetadata(innForeign, ColumnMetadata.named("INN_FOREIGN").withIndex(7).ofType(Types.VARCHAR).withSize(50));
        addMetadata(lastName, ColumnMetadata.named("LAST_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(60));
        addMetadata(medical, ColumnMetadata.named("MEDICAL").withIndex(15).ofType(Types.DECIMAL).withSize(1));
        addMetadata(middleName, ColumnMetadata.named("MIDDLE_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(60));
        addMetadata(oldId, ColumnMetadata.named("OLD_ID").withIndex(22).ofType(Types.DECIMAL).withSize(18));
        addMetadata(oldStatus, ColumnMetadata.named("OLD_STATUS").withIndex(23).ofType(Types.DECIMAL).withSize(1));
        addMetadata(pension, ColumnMetadata.named("PENSION").withIndex(14).ofType(Types.DECIMAL).withSize(1));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(18).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sex, ColumnMetadata.named("SEX").withIndex(5).ofType(Types.DECIMAL).withSize(1));
        addMetadata(snils, ColumnMetadata.named("SNILS").withIndex(8).ofType(Types.VARCHAR).withSize(14));
        addMetadata(social, ColumnMetadata.named("SOCIAL").withIndex(16).ofType(Types.DECIMAL).withSize(1));
        addMetadata(sourceId, ColumnMetadata.named("SOURCE_ID").withIndex(21).ofType(Types.DECIMAL).withSize(18));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(20).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(taxpayerState, ColumnMetadata.named("TAXPAYER_STATE").withIndex(9).ofType(Types.DECIMAL).withSize(18));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(19).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

