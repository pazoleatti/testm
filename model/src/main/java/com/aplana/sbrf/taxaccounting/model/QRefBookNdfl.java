package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookNdfl is a Querydsl query type for QRefBookNdfl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookNdfl extends com.querydsl.sql.RelationalPathBase<QRefBookNdfl> {

    private static final long serialVersionUID = -100884485;

    public static final QRefBookNdfl refBookNdfl = new QRefBookNdfl("REF_BOOK_NDFL");

    public final NumberPath<Long> departmentId = createNumber("departmentId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inn = createString("inn");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookNdfl> refBookNdflPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> refBookNdflDepartFk = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetParentFk = createInvForeignKey(id, "REF_BOOK_NDFL_ID");

    public QRefBookNdfl(String variable) {
        super(QRefBookNdfl.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_NDFL");
        addMetadata();
    }

    public QRefBookNdfl(String variable, String schema, String table) {
        super(QRefBookNdfl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookNdfl(Path<? extends QRefBookNdfl> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_NDFL");
        addMetadata();
    }

    public QRefBookNdfl(PathMetadata metadata) {
        super(QRefBookNdfl.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_NDFL");
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

