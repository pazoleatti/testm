package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNdflPersonIncome is a Querydsl query type for QNdflPersonIncome
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNdflPersonIncome extends com.querydsl.sql.RelationalPathBase<QNdflPersonIncome> {

    private static final long serialVersionUID = 1754359703;

    public static final QNdflPersonIncome ndflPersonIncome = new QNdflPersonIncome("NDFL_PERSON_INCOME");

    public final NumberPath<java.math.BigInteger> calculatedTax = createNumber("calculatedTax", java.math.BigInteger.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.sql.Timestamp> incomeAccruedDate = createDateTime("incomeAccruedDate", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> incomeAccruedSumm = createNumber("incomeAccruedSumm", java.math.BigDecimal.class);

    public final StringPath incomeCode = createString("incomeCode");

    public final DateTimePath<java.sql.Timestamp> incomePayoutDate = createDateTime("incomePayoutDate", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> incomePayoutSumm = createNumber("incomePayoutSumm", java.math.BigDecimal.class);

    public final StringPath incomeType = createString("incomeType");

    public final StringPath kpp = createString("kpp");

    public final NumberPath<Long> ndflPersonId = createNumber("ndflPersonId", Long.class);

    public final NumberPath<java.math.BigInteger> notHoldingTax = createNumber("notHoldingTax", java.math.BigInteger.class);

    public final StringPath oktmo = createString("oktmo");

    public final StringPath operationId = createString("operationId");

    public final NumberPath<java.math.BigInteger> overholdingTax = createNumber("overholdingTax", java.math.BigInteger.class);

    public final DateTimePath<java.sql.Timestamp> paymentDate = createDateTime("paymentDate", java.sql.Timestamp.class);

    public final StringPath paymentNumber = createString("paymentNumber");

    public final NumberPath<Long> refoundTax = createNumber("refoundTax", Long.class);

    public final NumberPath<java.math.BigInteger> rowNum = createNumber("rowNum", java.math.BigInteger.class);

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final NumberPath<java.math.BigDecimal> taxBase = createNumber("taxBase", java.math.BigDecimal.class);

    public final DateTimePath<java.sql.Timestamp> taxDate = createDateTime("taxDate", java.sql.Timestamp.class);

    public final NumberPath<Byte> taxRate = createNumber("taxRate", Byte.class);

    public final NumberPath<Long> taxSumm = createNumber("taxSumm", Long.class);

    public final DateTimePath<java.sql.Timestamp> taxTransferDate = createDateTime("taxTransferDate", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> totalDeductionsSumm = createNumber("totalDeductionsSumm", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigInteger> withholdingTax = createNumber("withholdingTax", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QNdflPersonIncome> ndflPersonIPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QNdflPersonIncome> ndflPersonIFkS = createForeignKey(sourceId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPerson> ndflPersonIFkNp = createForeignKey(ndflPersonId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonIncome> _ndflPersonIFkS = createInvForeignKey(id, "SOURCE_ID");

    public QNdflPersonIncome(String variable) {
        super(QNdflPersonIncome.class, forVariable(variable), "NDFL_UNSTABLE", "NDFL_PERSON_INCOME");
        addMetadata();
    }

    public QNdflPersonIncome(String variable, String schema, String table) {
        super(QNdflPersonIncome.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNdflPersonIncome(Path<? extends QNdflPersonIncome> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NDFL_PERSON_INCOME");
        addMetadata();
    }

    public QNdflPersonIncome(PathMetadata metadata) {
        super(QNdflPersonIncome.class, metadata, "NDFL_UNSTABLE", "NDFL_PERSON_INCOME");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(calculatedTax, ColumnMetadata.named("CALCULATED_TAX").withIndex(18).ofType(Types.DECIMAL).withSize(20));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(incomeAccruedDate, ColumnMetadata.named("INCOME_ACCRUED_DATE").withIndex(10).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(incomeAccruedSumm, ColumnMetadata.named("INCOME_ACCRUED_SUMM").withIndex(12).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(incomeCode, ColumnMetadata.named("INCOME_CODE").withIndex(6).ofType(Types.VARCHAR).withSize(4));
        addMetadata(incomePayoutDate, ColumnMetadata.named("INCOME_PAYOUT_DATE").withIndex(11).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(incomePayoutSumm, ColumnMetadata.named("INCOME_PAYOUT_SUMM").withIndex(13).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(incomeType, ColumnMetadata.named("INCOME_TYPE").withIndex(7).ofType(Types.VARCHAR).withSize(2));
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(9).ofType(Types.VARCHAR).withSize(9));
        addMetadata(ndflPersonId, ColumnMetadata.named("NDFL_PERSON_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(notHoldingTax, ColumnMetadata.named("NOT_HOLDING_TAX").withIndex(20).ofType(Types.DECIMAL).withSize(20));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(8).ofType(Types.VARCHAR).withSize(11));
        addMetadata(operationId, ColumnMetadata.named("OPERATION_ID").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(overholdingTax, ColumnMetadata.named("OVERHOLDING_TAX").withIndex(21).ofType(Types.DECIMAL).withSize(20));
        addMetadata(paymentDate, ColumnMetadata.named("PAYMENT_DATE").withIndex(24).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(paymentNumber, ColumnMetadata.named("PAYMENT_NUMBER").withIndex(25).ofType(Types.VARCHAR).withSize(20));
        addMetadata(refoundTax, ColumnMetadata.named("REFOUND_TAX").withIndex(22).ofType(Types.DECIMAL).withSize(15));
        addMetadata(rowNum, ColumnMetadata.named("ROW_NUM").withIndex(4).ofType(Types.DECIMAL).withSize(20));
        addMetadata(sourceId, ColumnMetadata.named("SOURCE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(taxBase, ColumnMetadata.named("TAX_BASE").withIndex(15).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(taxDate, ColumnMetadata.named("TAX_DATE").withIndex(17).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(taxRate, ColumnMetadata.named("TAX_RATE").withIndex(16).ofType(Types.DECIMAL).withSize(2));
        addMetadata(taxSumm, ColumnMetadata.named("TAX_SUMM").withIndex(26).ofType(Types.DECIMAL).withSize(10));
        addMetadata(taxTransferDate, ColumnMetadata.named("TAX_TRANSFER_DATE").withIndex(23).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(totalDeductionsSumm, ColumnMetadata.named("TOTAL_DEDUCTIONS_SUMM").withIndex(14).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(withholdingTax, ColumnMetadata.named("WITHHOLDING_TAX").withIndex(19).ofType(Types.DECIMAL).withSize(20));
    }

}

