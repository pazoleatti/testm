package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWri$AdvAsaRecoData is a Querydsl query type for QWri$AdvAsaRecoData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWri$AdvAsaRecoData extends com.querydsl.sql.RelationalPathBase<QWri$AdvAsaRecoData> {

    private static final long serialVersionUID = 793148792;

    public static final QWri$AdvAsaRecoData wri$AdvAsaRecoData = new QWri$AdvAsaRecoData("WRI$_ADV_ASA_RECO_DATA");

    public final NumberPath<java.math.BigInteger> alsp = createNumber("alsp", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> benefitType = createNumber("benefitType", java.math.BigInteger.class);

    public final StringPath c1 = createString("c1");

    public final StringPath c2 = createString("c2");

    public final StringPath c3 = createString("c3");

    public final NumberPath<java.math.BigInteger> chct = createNumber("chct", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cmdId = createNumber("cmdId", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> ctime = createDateTime("ctime", org.joda.time.DateTime.class);

    public final StringPath partname = createString("partname");

    public final NumberPath<java.math.BigInteger> rec = createNumber("rec", java.math.BigInteger.class);

    public final StringPath segname = createString("segname");

    public final StringPath segowner = createString("segowner");

    public final StringPath segtype = createString("segtype");

    public final NumberPath<java.math.BigInteger> taskId = createNumber("taskId", java.math.BigInteger.class);

    public final StringPath tsname = createString("tsname");

    public final NumberPath<java.math.BigInteger> usp = createNumber("usp", java.math.BigInteger.class);

    public QWri$AdvAsaRecoData(String variable) {
        super(QWri$AdvAsaRecoData.class, forVariable(variable), "SYS", "WRI$_ADV_ASA_RECO_DATA");
        addMetadata();
    }

    public QWri$AdvAsaRecoData(String variable, String schema, String table) {
        super(QWri$AdvAsaRecoData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWri$AdvAsaRecoData(Path<? extends QWri$AdvAsaRecoData> path) {
        super(path.getType(), path.getMetadata(), "SYS", "WRI$_ADV_ASA_RECO_DATA");
        addMetadata();
    }

    public QWri$AdvAsaRecoData(PathMetadata metadata) {
        super(QWri$AdvAsaRecoData.class, metadata, "SYS", "WRI$_ADV_ASA_RECO_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alsp, ColumnMetadata.named("ALSP").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(benefitType, ColumnMetadata.named("BENEFIT_TYPE").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c1, ColumnMetadata.named("C1").withIndex(14).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(c2, ColumnMetadata.named("C2").withIndex(15).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(c3, ColumnMetadata.named("C3").withIndex(16).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(chct, ColumnMetadata.named("CHCT").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cmdId, ColumnMetadata.named("CMD_ID").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ctime, ColumnMetadata.named("CTIME").withIndex(2).ofType(Types.TIMESTAMP).withSize(11).withDigits(6));
        addMetadata(partname, ColumnMetadata.named("PARTNAME").withIndex(6).ofType(Types.VARCHAR).withSize(100));
        addMetadata(rec, ColumnMetadata.named("REC").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(segname, ColumnMetadata.named("SEGNAME").withIndex(4).ofType(Types.VARCHAR).withSize(100));
        addMetadata(segowner, ColumnMetadata.named("SEGOWNER").withIndex(3).ofType(Types.VARCHAR).withSize(100));
        addMetadata(segtype, ColumnMetadata.named("SEGTYPE").withIndex(5).ofType(Types.VARCHAR).withSize(64));
        addMetadata(taskId, ColumnMetadata.named("TASK_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tsname, ColumnMetadata.named("TSNAME").withIndex(7).ofType(Types.VARCHAR).withSize(100));
        addMetadata(usp, ColumnMetadata.named("USP").withIndex(9).ofType(Types.DECIMAL).withSize(22));
    }

}

