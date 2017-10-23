package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNdflPerson is a Querydsl query type for QNdflPerson
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNdflPerson extends com.querydsl.sql.RelationalPathBase<QNdflPerson> {

    private static final long serialVersionUID = -1085033987;

    public static final QNdflPerson ndflPerson = new QNdflPerson("NDFL_PERSON");

    public final StringPath additionalData = createString("additionalData");

    public final StringPath address = createString("address");

    public final StringPath area = createString("area");

    public final DateTimePath<org.joda.time.LocalDateTime> birthDay = createDateTime("birthDay", org.joda.time.LocalDateTime.class);

    public final StringPath building = createString("building");

    public final StringPath citizenship = createString("citizenship");

    public final StringPath city = createString("city");

    public final StringPath countryCode = createString("countryCode");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final StringPath firstName = createString("firstName");

    public final StringPath flat = createString("flat");

    public final StringPath house = createString("house");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idDocNumber = createString("idDocNumber");

    public final StringPath idDocType = createString("idDocType");

    public final StringPath innForeign = createString("innForeign");

    public final StringPath innNp = createString("innNp");

    public final StringPath inp = createString("inp");

    public final StringPath lastName = createString("lastName");

    public final StringPath locality = createString("locality");

    public final StringPath middleName = createString("middleName");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final StringPath postIndex = createString("postIndex");

    public final StringPath regionCode = createString("regionCode");

    public final NumberPath<Long> rowNum = createNumber("rowNum", Long.class);

    public final StringPath snils = createString("snils");

    public final StringPath status = createString("status");

    public final StringPath street = createString("street");

    public final com.querydsl.sql.PrimaryKey<QNdflPerson> ndflPersonPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> ndflPersonFkPersonId = createForeignKey(personId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> ndflPersonFkD = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonPrepayment> _ndflPpFkNp = createInvForeignKey(id, "NDFL_PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonDeduction> _ndflPdFkNp = createInvForeignKey(id, "NDFL_PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonIncome> _ndflPersonIFkNp = createInvForeignKey(id, "NDFL_PERSON_ID");

    public final com.querydsl.sql.ForeignKey<QNdflReferences> _ndflPersonIdFk = createInvForeignKey(id, "NDFL_PERSON_ID");

    public QNdflPerson(String variable) {
        super(QNdflPerson.class, forVariable(variable), "NDFL_UNSTABLE", "NDFL_PERSON");
        addMetadata();
    }

    public QNdflPerson(String variable, String schema, String table) {
        super(QNdflPerson.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNdflPerson(String variable, String schema) {
        super(QNdflPerson.class, forVariable(variable), schema, "NDFL_PERSON");
        addMetadata();
    }

    public QNdflPerson(Path<? extends QNdflPerson> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NDFL_PERSON");
        addMetadata();
    }

    public QNdflPerson(PathMetadata metadata) {
        super(QNdflPerson.class, metadata, "NDFL_UNSTABLE", "NDFL_PERSON");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalData, ColumnMetadata.named("ADDITIONAL_DATA").withIndex(28).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(address, ColumnMetadata.named("ADDRESS").withIndex(27).ofType(Types.VARCHAR).withSize(255));
        addMetadata(area, ColumnMetadata.named("AREA").withIndex(19).ofType(Types.VARCHAR).withSize(60));
        addMetadata(birthDay, ColumnMetadata.named("BIRTH_DAY").withIndex(10).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(building, ColumnMetadata.named("BUILDING").withIndex(24).ofType(Types.VARCHAR).withSize(20));
        addMetadata(citizenship, ColumnMetadata.named("CITIZENSHIP").withIndex(11).ofType(Types.VARCHAR).withSize(3));
        addMetadata(city, ColumnMetadata.named("CITY").withIndex(20).ofType(Types.VARCHAR).withSize(50));
        addMetadata(countryCode, ColumnMetadata.named("COUNTRY_CODE").withIndex(26).ofType(Types.VARCHAR).withSize(10));
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(firstName, ColumnMetadata.named("FIRST_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(36));
        addMetadata(flat, ColumnMetadata.named("FLAT").withIndex(25).ofType(Types.VARCHAR).withSize(10));
        addMetadata(house, ColumnMetadata.named("HOUSE").withIndex(23).ofType(Types.VARCHAR).withSize(20));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(idDocNumber, ColumnMetadata.named("ID_DOC_NUMBER").withIndex(15).ofType(Types.VARCHAR).withSize(25));
        addMetadata(idDocType, ColumnMetadata.named("ID_DOC_TYPE").withIndex(14).ofType(Types.VARCHAR).withSize(2));
        addMetadata(innForeign, ColumnMetadata.named("INN_FOREIGN").withIndex(13).ofType(Types.VARCHAR).withSize(50));
        addMetadata(innNp, ColumnMetadata.named("INN_NP").withIndex(12).ofType(Types.VARCHAR).withSize(12));
        addMetadata(inp, ColumnMetadata.named("INP").withIndex(5).ofType(Types.VARCHAR).withSize(25));
        addMetadata(lastName, ColumnMetadata.named("LAST_NAME").withIndex(7).ofType(Types.VARCHAR).withSize(36));
        addMetadata(locality, ColumnMetadata.named("LOCALITY").withIndex(21).ofType(Types.VARCHAR).withSize(50));
        addMetadata(middleName, ColumnMetadata.named("MIDDLE_NAME").withIndex(9).ofType(Types.VARCHAR).withSize(36));
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(postIndex, ColumnMetadata.named("POST_INDEX").withIndex(17).ofType(Types.VARCHAR).withSize(6));
        addMetadata(regionCode, ColumnMetadata.named("REGION_CODE").withIndex(18).ofType(Types.VARCHAR).withSize(2));
        addMetadata(rowNum, ColumnMetadata.named("ROW_NUM").withIndex(4).ofType(Types.DECIMAL).withSize(10));
        addMetadata(snils, ColumnMetadata.named("SNILS").withIndex(6).ofType(Types.VARCHAR).withSize(14));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(16).ofType(Types.VARCHAR).withSize(1));
        addMetadata(street, ColumnMetadata.named("STREET").withIndex(22).ofType(Types.VARCHAR).withSize(120));
    }

}

