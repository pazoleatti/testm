package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookMakeCalc is a Querydsl query type for QRefBookMakeCalc
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookMakeCalc extends com.querydsl.sql.RelationalPathBase<QRefBookMakeCalc> {

    private static final long serialVersionUID = 1813500130;

    public static final QRefBookMakeCalc refBookMakeCalc = new QRefBookMakeCalc("REF_BOOK_MAKE_CALC");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.DateTime> version = createDateTime("version", org.joda.time.DateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookMakeCalc> refBookMakeCalcPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetTypeFk = createInvForeignKey(id, "TYPE");

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetTypeFk = createInvForeignKey(id, "TYPE");

    public QRefBookMakeCalc(String variable) {
        super(QRefBookMakeCalc.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_MAKE_CALC");
        addMetadata();
    }

    public QRefBookMakeCalc(String variable, String schema, String table) {
        super(QRefBookMakeCalc.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookMakeCalc(Path<? extends QRefBookMakeCalc> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_MAKE_CALC");
        addMetadata();
    }

    public QRefBookMakeCalc(PathMetadata metadata) {
        super(QRefBookMakeCalc.class, metadata, "NDFL_1_0", "REF_BOOK_MAKE_CALC");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

