package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookPersonCategory is a Querydsl query type for QRefBookPersonCategory
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookPersonCategory extends com.querydsl.sql.RelationalPathBase<QRefBookPersonCategory> {

    private static final long serialVersionUID = 2133671858;

    public static final QRefBookPersonCategory refBookPersonCategory = new QRefBookPersonCategory("REF_BOOK_PERSON_CATEGORY");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<java.sql.Timestamp> version = createDateTime("version", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookPersonCategory> refBookPersonCategoryPk = createPrimaryKey(id);

    public QRefBookPersonCategory(String variable) {
        super(QRefBookPersonCategory.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_PERSON_CATEGORY");
        addMetadata();
    }

    public QRefBookPersonCategory(String variable, String schema, String table) {
        super(QRefBookPersonCategory.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookPersonCategory(Path<? extends QRefBookPersonCategory> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_PERSON_CATEGORY");
        addMetadata();
    }

    public QRefBookPersonCategory(PathMetadata metadata) {
        super(QRefBookPersonCategory.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_PERSON_CATEGORY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(5).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

