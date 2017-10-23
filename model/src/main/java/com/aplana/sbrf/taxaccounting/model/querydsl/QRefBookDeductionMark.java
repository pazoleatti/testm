package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookDeductionMark is a Querydsl query type for QRefBookDeductionMark
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookDeductionMark extends com.querydsl.sql.RelationalPathBase<QRefBookDeductionMark> {

    private static final long serialVersionUID = -1411707998;

    public static final QRefBookDeductionMark refBookDeductionMark = new QRefBookDeductionMark("REF_BOOK_DEDUCTION_MARK");

    public final NumberPath<Byte> code = createNumber("code", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookDeductionMark> refBookDeductionMarkPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookDeductionType> _refBookDeducTypeMarkFk = createInvForeignKey(id, "DEDUCTION_MARK");

    public QRefBookDeductionMark(String variable) {
        super(QRefBookDeductionMark.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_MARK");
        addMetadata();
    }

    public QRefBookDeductionMark(String variable, String schema, String table) {
        super(QRefBookDeductionMark.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookDeductionMark(String variable, String schema) {
        super(QRefBookDeductionMark.class, forVariable(variable), schema, "REF_BOOK_DEDUCTION_MARK");
        addMetadata();
    }

    public QRefBookDeductionMark(Path<? extends QRefBookDeductionMark> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_MARK");
        addMetadata();
    }

    public QRefBookDeductionMark(PathMetadata metadata) {
        super(QRefBookDeductionMark.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_MARK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(30).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

