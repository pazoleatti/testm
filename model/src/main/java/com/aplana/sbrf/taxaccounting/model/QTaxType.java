package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTaxType is a Querydsl query type for QTaxType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTaxType extends com.querydsl.sql.RelationalPathBase<QTaxType> {

    private static final long serialVersionUID = 407043528;

    public static final QTaxType taxType = new QTaxType("TAX_TYPE");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QTaxType> taxTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTaxPeriod> _taxPeriodFkTaxtype = createInvForeignKey(id, "TAX_TYPE");

    public final com.querydsl.sql.ForeignKey<QDeclarationType> _declarationTypeFkTaxtype = createInvForeignKey(id, "TAX_TYPE");

    public final com.querydsl.sql.ForeignKey<QFormType> _formTypeFkTaxtype = createInvForeignKey(id, "TAX_TYPE");

    public QTaxType(String variable) {
        super(QTaxType.class, forVariable(variable), "NDFL_UNSTABLE", "TAX_TYPE");
        addMetadata();
    }

    public QTaxType(String variable, String schema, String table) {
        super(QTaxType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTaxType(Path<? extends QTaxType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TAX_TYPE");
        addMetadata();
    }

    public QTaxType(PathMetadata metadata) {
        super(QTaxType.class, metadata, "NDFL_UNSTABLE", "TAX_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
    }

}

