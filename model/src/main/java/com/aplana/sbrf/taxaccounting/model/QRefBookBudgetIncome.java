package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookBudgetIncome is a Querydsl query type for QRefBookBudgetIncome
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookBudgetIncome extends com.querydsl.sql.RelationalPathBase<QRefBookBudgetIncome> {

    private static final long serialVersionUID = 1307681101;

    public static final QRefBookBudgetIncome refBookBudgetIncome = new QRefBookBudgetIncome("REF_BOOK_BUDGET_INCOME");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lev = createString("lev");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookBudgetIncome> refBookBudgetIncomePk = createPrimaryKey(id);

    public QRefBookBudgetIncome(String variable) {
        super(QRefBookBudgetIncome.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_BUDGET_INCOME");
        addMetadata();
    }

    public QRefBookBudgetIncome(String variable, String schema, String table) {
        super(QRefBookBudgetIncome.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookBudgetIncome(Path<? extends QRefBookBudgetIncome> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_BUDGET_INCOME");
        addMetadata();
    }

    public QRefBookBudgetIncome(PathMetadata metadata) {
        super(QRefBookBudgetIncome.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_BUDGET_INCOME");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(lev, ColumnMetadata.named("LEV").withIndex(7).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

