package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookPresentPlace is a Querydsl query type for QRefBookPresentPlace
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookPresentPlace extends com.querydsl.sql.RelationalPathBase<QRefBookPresentPlace> {

    private static final long serialVersionUID = -321824501;

    public static final QRefBookPresentPlace refBookPresentPlace = new QRefBookPresentPlace("REF_BOOK_PRESENT_PLACE");

    public final StringPath code = createString("code");

    public final NumberPath<Byte> forFond = createNumber("forFond", Byte.class);

    public final NumberPath<Byte> forNdfl = createNumber("forNdfl", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookPresentPlace> refBookPresentPlacePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetPresPlFk = createInvForeignKey(id, "PRESENT_PLACE");

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetPresPlFk = createInvForeignKey(id, "PRESENT_PLACE");

    public QRefBookPresentPlace(String variable) {
        super(QRefBookPresentPlace.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_PRESENT_PLACE");
        addMetadata();
    }

    public QRefBookPresentPlace(String variable, String schema, String table) {
        super(QRefBookPresentPlace.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookPresentPlace(Path<? extends QRefBookPresentPlace> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_PRESENT_PLACE");
        addMetadata();
    }

    public QRefBookPresentPlace(PathMetadata metadata) {
        super(QRefBookPresentPlace.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_PRESENT_PLACE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(forFond, ColumnMetadata.named("FOR_FOND").withIndex(8).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(forNdfl, ColumnMetadata.named("FOR_NDFL").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

