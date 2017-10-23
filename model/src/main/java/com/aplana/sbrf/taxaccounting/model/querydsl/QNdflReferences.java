package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNdflReferences is a Querydsl query type for QNdflReferences
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNdflReferences extends com.querydsl.sql.RelationalPathBase<QNdflReferences> {

    private static final long serialVersionUID = -733926096;

    public static final QNdflReferences ndflReferences = new QNdflReferences("NDFL_REFERENCES");

    public final DateTimePath<org.joda.time.LocalDateTime> birthday = createDateTime("birthday", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final StringPath errtext = createString("errtext");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastname = createString("lastname");

    public final StringPath name = createString("name");

    public final NumberPath<Long> ndflPersonId = createNumber("ndflPersonId", Long.class);

    public final NumberPath<Long> num = createNumber("num", Long.class);

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final StringPath surname = createString("surname");

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QNdflReferences> ndflReferencesPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> ndflRefersPersonFk = createForeignKey(personId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> ndflRefersDeclDataFk = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPerson> ndflPersonIdFk = createForeignKey(ndflPersonId, "ID");

    public QNdflReferences(String variable) {
        super(QNdflReferences.class, forVariable(variable), "NDFL_UNSTABLE", "NDFL_REFERENCES");
        addMetadata();
    }

    public QNdflReferences(String variable, String schema, String table) {
        super(QNdflReferences.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNdflReferences(String variable, String schema) {
        super(QNdflReferences.class, forVariable(variable), schema, "NDFL_REFERENCES");
        addMetadata();
    }

    public QNdflReferences(Path<? extends QNdflReferences> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NDFL_REFERENCES");
        addMetadata();
    }

    public QNdflReferences(PathMetadata metadata) {
        super(QNdflReferences.class, metadata, "NDFL_UNSTABLE", "NDFL_REFERENCES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(birthday, ColumnMetadata.named("BIRTHDAY").withIndex(11).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(errtext, ColumnMetadata.named("ERRTEXT").withIndex(12).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(lastname, ColumnMetadata.named("LASTNAME").withIndex(10).ofType(Types.VARCHAR).withSize(60));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(9).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(ndflPersonId, ColumnMetadata.named("NDFL_PERSON_ID").withIndex(13).ofType(Types.DECIMAL).withSize(18));
        addMetadata(num, ColumnMetadata.named("NUM").withIndex(7).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(surname, ColumnMetadata.named("SURNAME").withIndex(8).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

