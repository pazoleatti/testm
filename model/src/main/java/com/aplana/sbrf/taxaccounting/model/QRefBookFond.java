package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookFond is a Querydsl query type for QRefBookFond
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookFond extends com.querydsl.sql.RelationalPathBase<QRefBookFond> {

    private static final long serialVersionUID = -101112002;

    public static final QRefBookFond refBookFond = new QRefBookFond("REF_BOOK_FOND");

    public final NumberPath<Long> departmentId = createNumber("departmentId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inn = createString("inn");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.DateTime> version = createDateTime("version", org.joda.time.DateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookFond> refBookFondPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> refBookFondDepartFk = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetParentFk = createInvForeignKey(id, "REF_BOOK_FOND_ID");

    public QRefBookFond(String variable) {
        super(QRefBookFond.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_FOND");
        addMetadata();
    }

    public QRefBookFond(String variable, String schema, String table) {
        super(QRefBookFond.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookFond(Path<? extends QRefBookFond> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_FOND");
        addMetadata();
    }

    public QRefBookFond(PathMetadata metadata) {
        super(QRefBookFond.class, metadata, "NDFL_1_0", "REF_BOOK_FOND");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(inn, ColumnMetadata.named("INN").withIndex(6).ofType(Types.VARCHAR).withSize(12));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

