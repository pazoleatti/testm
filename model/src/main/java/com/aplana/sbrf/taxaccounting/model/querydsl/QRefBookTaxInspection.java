package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookTaxInspection is a Querydsl query type for QRefBookTaxInspection
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookTaxInspection extends com.querydsl.sql.RelationalPathBase<QRefBookTaxInspection> {

    private static final long serialVersionUID = 1917123151;

    public static final QRefBookTaxInspection refBookTaxInspection = new QRefBookTaxInspection("REF_BOOK_TAX_INSPECTION");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QRefBookTaxInspection> refBookTaxInspectionPk = createPrimaryKey(id);

    public QRefBookTaxInspection(String variable) {
        super(QRefBookTaxInspection.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_TAX_INSPECTION");
        addMetadata();
    }

    public QRefBookTaxInspection(String variable, String schema, String table) {
        super(QRefBookTaxInspection.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookTaxInspection(String variable, String schema) {
        super(QRefBookTaxInspection.class, forVariable(variable), schema, "REF_BOOK_TAX_INSPECTION");
        addMetadata();
    }

    public QRefBookTaxInspection(Path<? extends QRefBookTaxInspection> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_TAX_INSPECTION");
        addMetadata();
    }

    public QRefBookTaxInspection(PathMetadata metadata) {
        super(QRefBookTaxInspection.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_TAX_INSPECTION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(250).notNull());
    }

}

