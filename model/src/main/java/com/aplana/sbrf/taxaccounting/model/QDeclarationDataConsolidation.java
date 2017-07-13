package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationDataConsolidation is a Querydsl query type for QDeclarationDataConsolidation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationDataConsolidation extends com.querydsl.sql.RelationalPathBase<QDeclarationDataConsolidation> {

    private static final long serialVersionUID = -1142059035;

    public static final QDeclarationDataConsolidation declarationDataConsolidation = new QDeclarationDataConsolidation("DECLARATION_DATA_CONSOLIDATION");

    public final NumberPath<Long> sourceDeclarationDataId = createNumber("sourceDeclarationDataId", Long.class);

    public final NumberPath<Long> targetDeclarationDataId = createNumber("targetDeclarationDataId", Long.class);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> declDataConsolidationFkSrc = createForeignKey(sourceDeclarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> declDataConsolidationFkTgt = createForeignKey(targetDeclarationDataId, "ID");

    public QDeclarationDataConsolidation(String variable) {
        super(QDeclarationDataConsolidation.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_DATA_CONSOLIDATION");
        addMetadata();
    }

    public QDeclarationDataConsolidation(String variable, String schema, String table) {
        super(QDeclarationDataConsolidation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationDataConsolidation(Path<? extends QDeclarationDataConsolidation> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_DATA_CONSOLIDATION");
        addMetadata();
    }

    public QDeclarationDataConsolidation(PathMetadata metadata) {
        super(QDeclarationDataConsolidation.class, metadata, "NDFL_UNSTABLE", "DECLARATION_DATA_CONSOLIDATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sourceDeclarationDataId, ColumnMetadata.named("SOURCE_DECLARATION_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18));
        addMetadata(targetDeclarationDataId, ColumnMetadata.named("TARGET_DECLARATION_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

