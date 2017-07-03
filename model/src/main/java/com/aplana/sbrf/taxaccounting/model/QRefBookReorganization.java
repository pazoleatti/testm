package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookReorganization is a Querydsl query type for QRefBookReorganization
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookReorganization extends com.querydsl.sql.RelationalPathBase<QRefBookReorganization> {

    private static final long serialVersionUID = -1858539739;

    public static final QRefBookReorganization refBookReorganization = new QRefBookReorganization("REF_BOOK_REORGANIZATION");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.DateTime> version = createDateTime("version", org.joda.time.DateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookReorganization> refBookReorganizationPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetReCodeFk = createInvForeignKey(id, "REORG_FORM_CODE");

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetReCodeFk = createInvForeignKey(id, "REORG_FORM_CODE");

    public QRefBookReorganization(String variable) {
        super(QRefBookReorganization.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_REORGANIZATION");
        addMetadata();
    }

    public QRefBookReorganization(String variable, String schema, String table) {
        super(QRefBookReorganization.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookReorganization(Path<? extends QRefBookReorganization> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_REORGANIZATION");
        addMetadata();
    }

    public QRefBookReorganization(PathMetadata metadata) {
        super(QRefBookReorganization.class, metadata, "NDFL_1_0", "REF_BOOK_REORGANIZATION");
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

