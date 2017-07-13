package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataConsolidation is a Querydsl query type for QFormDataConsolidation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataConsolidation extends com.querydsl.sql.RelationalPathBase<QFormDataConsolidation> {

    private static final long serialVersionUID = -906412735;

    public static final QFormDataConsolidation formDataConsolidation = new QFormDataConsolidation("FORM_DATA_CONSOLIDATION");

    public final NumberPath<Integer> sourceFormDataId = createNumber("sourceFormDataId", Integer.class);

    public final NumberPath<Integer> targetFormDataId = createNumber("targetFormDataId", Integer.class);

    public final com.querydsl.sql.ForeignKey<QFormData> formDataConsolidationFkTgt = createForeignKey(targetFormDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormData> formDataConsolidationFkSrc = createForeignKey(sourceFormDataId, "ID");

    public QFormDataConsolidation(String variable) {
        super(QFormDataConsolidation.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_DATA_CONSOLIDATION");
        addMetadata();
    }

    public QFormDataConsolidation(String variable, String schema, String table) {
        super(QFormDataConsolidation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataConsolidation(Path<? extends QFormDataConsolidation> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_DATA_CONSOLIDATION");
        addMetadata();
    }

    public QFormDataConsolidation(PathMetadata metadata) {
        super(QFormDataConsolidation.class, metadata, "NDFL_UNSTABLE", "FORM_DATA_CONSOLIDATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sourceFormDataId, ColumnMetadata.named("SOURCE_FORM_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9));
        addMetadata(targetFormDataId, ColumnMetadata.named("TARGET_FORM_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

