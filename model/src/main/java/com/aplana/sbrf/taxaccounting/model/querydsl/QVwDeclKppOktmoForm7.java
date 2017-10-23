package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVwDeclKppOktmoForm7 is a Querydsl query type for QVwDeclKppOktmoForm7
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QVwDeclKppOktmoForm7 extends com.querydsl.sql.RelationalPathBase<QVwDeclKppOktmoForm7> {

    private static final long serialVersionUID = -505185779;

    public static final QVwDeclKppOktmoForm7 vwDeclKppOktmoForm7 = new QVwDeclKppOktmoForm7("VW_DECL_KPP_OKTMO_FORM7");

    public final DateTimePath<org.joda.time.LocalDateTime> correctionDate = createDateTime("correctionDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> formKind = createNumber("formKind", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kpp = createString("kpp");

    public final StringPath name = createString("name");

    public final StringPath note = createString("note");

    public final StringPath oktmo = createString("oktmo");

    public final NumberPath<java.math.BigInteger> persCnt = createNumber("persCnt", java.math.BigInteger.class);

    public final NumberPath<Integer> reportPeriodId = createNumber("reportPeriodId", Integer.class);

    public QVwDeclKppOktmoForm7(String variable) {
        super(QVwDeclKppOktmoForm7.class, forVariable(variable), "NDFL_UNSTABLE", "VW_DECL_KPP_OKTMO_FORM7");
        addMetadata();
    }

    public QVwDeclKppOktmoForm7(String variable, String schema, String table) {
        super(QVwDeclKppOktmoForm7.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVwDeclKppOktmoForm7(String variable, String schema) {
        super(QVwDeclKppOktmoForm7.class, forVariable(variable), schema, "VW_DECL_KPP_OKTMO_FORM7");
        addMetadata();
    }

    public QVwDeclKppOktmoForm7(Path<? extends QVwDeclKppOktmoForm7> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "VW_DECL_KPP_OKTMO_FORM7");
        addMetadata();
    }

    public QVwDeclKppOktmoForm7(PathMetadata metadata) {
        super(QVwDeclKppOktmoForm7.class, metadata, "NDFL_UNSTABLE", "VW_DECL_KPP_OKTMO_FORM7");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(correctionDate, ColumnMetadata.named("CORRECTION_DATE").withIndex(7).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(formKind, ColumnMetadata.named("FORM_KIND").withIndex(5).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18));
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(2).ofType(Types.VARCHAR).withSize(9));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(4).ofType(Types.VARCHAR).withSize(512));
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(9).ofType(Types.VARCHAR).withSize(512));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(3).ofType(Types.VARCHAR).withSize(11));
        addMetadata(persCnt, ColumnMetadata.named("PERS_CNT").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(reportPeriodId, ColumnMetadata.named("REPORT_PERIOD_ID").withIndex(6).ofType(Types.DECIMAL).withSize(9));
    }

}

