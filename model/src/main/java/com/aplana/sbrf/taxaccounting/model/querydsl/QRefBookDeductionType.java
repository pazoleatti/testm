package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookDeductionType is a Querydsl query type for QRefBookDeductionType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookDeductionType extends com.querydsl.sql.RelationalPathBase<QRefBookDeductionType> {

    private static final long serialVersionUID = -1411476465;

    public static final QRefBookDeductionType refBookDeductionType = new QRefBookDeductionType("REF_BOOK_DEDUCTION_TYPE");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> deductionMark = createNumber("deductionMark", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookDeductionType> refBookDeductionTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookDeductionMark> refBookDeducTypeMarkFk = createForeignKey(deductionMark, "ID");

    public QRefBookDeductionType(String variable) {
        super(QRefBookDeductionType.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_TYPE");
        addMetadata();
    }

    public QRefBookDeductionType(String variable, String schema, String table) {
        super(QRefBookDeductionType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookDeductionType(String variable, String schema) {
        super(QRefBookDeductionType.class, forVariable(variable), schema, "REF_BOOK_DEDUCTION_TYPE");
        addMetadata();
    }

    public QRefBookDeductionType(Path<? extends QRefBookDeductionType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_TYPE");
        addMetadata();
    }

    public QRefBookDeductionType(PathMetadata metadata) {
        super(QRefBookDeductionType.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_DEDUCTION_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(deductionMark, ColumnMetadata.named("DEDUCTION_MARK").withIndex(7).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}
