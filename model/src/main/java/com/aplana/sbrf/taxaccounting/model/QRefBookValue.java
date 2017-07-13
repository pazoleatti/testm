package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookValue is a Querydsl query type for QRefBookValue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookValue extends com.querydsl.sql.RelationalPathBase<QRefBookValue> {

    private static final long serialVersionUID = 1174853202;

    public static final QRefBookValue refBookValue = new QRefBookValue("REF_BOOK_VALUE");

    public final NumberPath<Long> attributeId = createNumber("attributeId", Long.class);

    public final DateTimePath<java.sql.Timestamp> dateValue = createDateTime("dateValue", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> numberValue = createNumber("numberValue", java.math.BigDecimal.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Long> referenceValue = createNumber("referenceValue", Long.class);

    public final StringPath stringValue = createString("stringValue");

    public final com.querydsl.sql.PrimaryKey<QRefBookValue> refBookValuePk = createPrimaryKey(attributeId, recordId);

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> refBookValueFkAttributeId = createForeignKey(attributeId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookRecord> refBookValueFkRecordId = createForeignKey(recordId, "ID");

    public QRefBookValue(String variable) {
        super(QRefBookValue.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_VALUE");
        addMetadata();
    }

    public QRefBookValue(String variable, String schema, String table) {
        super(QRefBookValue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookValue(Path<? extends QRefBookValue> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_VALUE");
        addMetadata();
    }

    public QRefBookValue(PathMetadata metadata) {
        super(QRefBookValue.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_VALUE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(dateValue, ColumnMetadata.named("DATE_VALUE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(numberValue, ColumnMetadata.named("NUMBER_VALUE").withIndex(4).ofType(Types.DECIMAL).withSize(38).withDigits(19));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(referenceValue, ColumnMetadata.named("REFERENCE_VALUE").withIndex(6).ofType(Types.DECIMAL).withSize(18));
        addMetadata(stringValue, ColumnMetadata.named("STRING_VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(4000));
    }

}

