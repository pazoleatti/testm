package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookHardWork is a Querydsl query type for QRefBookHardWork
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookHardWork extends com.querydsl.sql.RelationalPathBase<QRefBookHardWork> {

    private static final long serialVersionUID = 1889473051;

    public static final QRefBookHardWork refBookHardWork = new QRefBookHardWork("REF_BOOK_HARD_WORK");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookHardWork> refBookHardWorkPk = createPrimaryKey(id);

    public QRefBookHardWork(String variable) {
        super(QRefBookHardWork.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_HARD_WORK");
        addMetadata();
    }

    public QRefBookHardWork(String variable, String schema, String table) {
        super(QRefBookHardWork.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookHardWork(Path<? extends QRefBookHardWork> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_HARD_WORK");
        addMetadata();
    }

    public QRefBookHardWork(PathMetadata metadata) {
        super(QRefBookHardWork.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_HARD_WORK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

