package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookIncomeKind is a Querydsl query type for QRefBookIncomeKind
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookIncomeKind extends com.querydsl.sql.RelationalPathBase<QRefBookIncomeKind> {

    private static final long serialVersionUID = -1786448740;

    public static final QRefBookIncomeKind refBookIncomeKind = new QRefBookIncomeKind("REF_BOOK_INCOME_KIND");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> incomeTypeId = createNumber("incomeTypeId", Long.class);

    public final StringPath mark = createString("mark");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QRefBookIncomeKind> refBookIncomeKindPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookIncomeType> refBookInckindInctypeFk = createForeignKey(incomeTypeId, "ID");

    public QRefBookIncomeKind(String variable) {
        super(QRefBookIncomeKind.class, forVariable(variable), "NDFL_1_0", "REF_BOOK_INCOME_KIND");
        addMetadata();
    }

    public QRefBookIncomeKind(String variable, String schema, String table) {
        super(QRefBookIncomeKind.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookIncomeKind(Path<? extends QRefBookIncomeKind> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "REF_BOOK_INCOME_KIND");
        addMetadata();
    }

    public QRefBookIncomeKind(PathMetadata metadata) {
        super(QRefBookIncomeKind.class, metadata, "NDFL_1_0", "REF_BOOK_INCOME_KIND");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(incomeTypeId, ColumnMetadata.named("INCOME_TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(mark, ColumnMetadata.named("MARK").withIndex(3).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(4).ofType(Types.VARCHAR).withSize(255));
    }

}

