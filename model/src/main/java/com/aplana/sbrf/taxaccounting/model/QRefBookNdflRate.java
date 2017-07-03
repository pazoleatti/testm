package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookNdflRate is a Querydsl query type for QRefBookNdflRate
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookNdflRate extends com.querydsl.sql.RelationalPathBase<QRefBookNdflRate> {

    private static final long serialVersionUID = 1787620219;

    public static final QRefBookNdflRate refBookNdflRate = new QRefBookNdflRate("REF_BOOK_NDFL_RATE");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath rate = createString("rate");

    public final com.querydsl.sql.PrimaryKey<QRefBookNdflRate> refBookNdflRatePk = createPrimaryKey(id);

    public QRefBookNdflRate(String variable) {
        super(QRefBookNdflRate.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_NDFL_RATE");
        addMetadata();
    }

    public QRefBookNdflRate(String variable, String schema, String table) {
        super(QRefBookNdflRate.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookNdflRate(Path<? extends QRefBookNdflRate> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_NDFL_RATE");
        addMetadata();
    }

    public QRefBookNdflRate(PathMetadata metadata) {
        super(QRefBookNdflRate.class, metadata, "NDFL_1_0", "REF_BOOK_NDFL_RATE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(rate, ColumnMetadata.named("RATE").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

