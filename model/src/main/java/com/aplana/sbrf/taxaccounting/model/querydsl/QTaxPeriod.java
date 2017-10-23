package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTaxPeriod is a Querydsl query type for QTaxPeriod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTaxPeriod extends com.querydsl.sql.RelationalPathBase<QTaxPeriod> {

    private static final long serialVersionUID = 54894912;

    public static final QTaxPeriod taxPeriod = new QTaxPeriod("TAX_PERIOD");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath taxType = createString("taxType");

    public final NumberPath<Short> year = createNumber("year", Short.class);

    public final com.querydsl.sql.PrimaryKey<QTaxPeriod> taxPeriodPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTaxType> taxPeriodFkTaxtype = createForeignKey(taxType, "ID");

    public final com.querydsl.sql.ForeignKey<QReportPeriod> _reportPeriodFkTaxperiod = createInvForeignKey(id, "TAX_PERIOD_ID");

    public QTaxPeriod(String variable) {
        super(QTaxPeriod.class, forVariable(variable), "NDFL_UNSTABLE", "TAX_PERIOD");
        addMetadata();
    }

    public QTaxPeriod(String variable, String schema, String table) {
        super(QTaxPeriod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTaxPeriod(String variable, String schema) {
        super(QTaxPeriod.class, forVariable(variable), schema, "TAX_PERIOD");
        addMetadata();
    }

    public QTaxPeriod(Path<? extends QTaxPeriod> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TAX_PERIOD");
        addMetadata();
    }

    public QTaxPeriod(PathMetadata metadata) {
        super(QTaxPeriod.class, metadata, "NDFL_UNSTABLE", "TAX_PERIOD");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(taxType, ColumnMetadata.named("TAX_TYPE").withIndex(2).ofType(Types.CHAR).withSize(1).notNull());
        addMetadata(year, ColumnMetadata.named("YEAR").withIndex(3).ofType(Types.DECIMAL).withSize(4).notNull());
    }

}

