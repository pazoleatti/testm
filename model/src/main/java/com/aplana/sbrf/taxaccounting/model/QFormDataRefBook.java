package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataRefBook is a Querydsl query type for QFormDataRefBook
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataRefBook extends com.querydsl.sql.RelationalPathBase<QFormDataRefBook> {

    private static final long serialVersionUID = 1398294769;

    public static final QFormDataRefBook formDataRefBook = new QFormDataRefBook("FORM_DATA_REF_BOOK");

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Long> refBookId = createNumber("refBookId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QFormDataRefBook> formDataRefBookPk = createPrimaryKey(formDataId, recordId, refBookId);

    public final com.querydsl.sql.ForeignKey<QFormData> formDataRefBookFkFormdata = createForeignKey(formDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBook> formDataRefBookFkRefbook = createForeignKey(refBookId, "ID");

    public QFormDataRefBook(String variable) {
        super(QFormDataRefBook.class, forVariable(variable), "NDFL_1_0", "FORM_DATA_REF_BOOK");
        addMetadata();
    }

    public QFormDataRefBook(String variable, String schema, String table) {
        super(QFormDataRefBook.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataRefBook(Path<? extends QFormDataRefBook> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_DATA_REF_BOOK");
        addMetadata();
    }

    public QFormDataRefBook(PathMetadata metadata) {
        super(QFormDataRefBook.class, metadata, "NDFL_1_0", "FORM_DATA_REF_BOOK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(refBookId, ColumnMetadata.named("REF_BOOK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

