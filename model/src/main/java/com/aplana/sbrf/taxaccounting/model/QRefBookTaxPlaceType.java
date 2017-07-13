package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookTaxPlaceType is a Querydsl query type for QRefBookTaxPlaceType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookTaxPlaceType extends com.querydsl.sql.RelationalPathBase<QRefBookTaxPlaceType> {

    private static final long serialVersionUID = 269647221;

    public static final QRefBookTaxPlaceType refBookTaxPlaceType = new QRefBookTaxPlaceType("REF_BOOK_TAX_PLACE_TYPE");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookTaxPlaceType> refBookTaxPlaceTypePk = createPrimaryKey(id);

    public QRefBookTaxPlaceType(String variable) {
        super(QRefBookTaxPlaceType.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_TAX_PLACE_TYPE");
        addMetadata();
    }

    public QRefBookTaxPlaceType(String variable, String schema, String table) {
        super(QRefBookTaxPlaceType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookTaxPlaceType(Path<? extends QRefBookTaxPlaceType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_TAX_PLACE_TYPE");
        addMetadata();
    }

    public QRefBookTaxPlaceType(PathMetadata metadata) {
        super(QRefBookTaxPlaceType.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_TAX_PLACE_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

