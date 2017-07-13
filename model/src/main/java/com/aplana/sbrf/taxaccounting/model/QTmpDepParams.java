package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTmpDepParams is a Querydsl query type for QTmpDepParams
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTmpDepParams extends com.querydsl.sql.RelationalPathBase<QTmpDepParams> {

    private static final long serialVersionUID = -1465830213;

    public static final QTmpDepParams tmpDepParams = new QTmpDepParams("TMP_DEP_PARAMS");

    public final StringPath depcode = createString("depcode");

    public final StringPath docname = createString("docname");

    public final StringPath inn = createString("inn");

    public final StringPath kpp = createString("kpp");

    public final StringPath lastname = createString("lastname");

    public final StringPath name = createString("name");

    public final StringPath oktmo = createString("oktmo");

    public final StringPath orgname = createString("orgname");

    public final StringPath phone = createString("phone");

    public final StringPath place = createString("place");

    public final StringPath reorgcode = createString("reorgcode");

    public final NumberPath<java.math.BigInteger> rowNum = createNumber("rowNum", java.math.BigInteger.class);

    public final StringPath sign = createString("sign");

    public final StringPath surname = createString("surname");

    public final StringPath taxEnd = createString("taxEnd");

    public final StringPath titname = createString("titname");

    public QTmpDepParams(String variable) {
        super(QTmpDepParams.class, forVariable(variable), "NDFL_UNSTABLE", "TMP_DEP_PARAMS");
        addMetadata();
    }

    public QTmpDepParams(String variable, String schema, String table) {
        super(QTmpDepParams.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTmpDepParams(Path<? extends QTmpDepParams> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "TMP_DEP_PARAMS");
        addMetadata();
    }

    public QTmpDepParams(PathMetadata metadata) {
        super(QTmpDepParams.class, metadata, "NDFL_UNSTABLE", "TMP_DEP_PARAMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(depcode, ColumnMetadata.named("DEPCODE").withIndex(1).ofType(Types.VARCHAR).withSize(100));
        addMetadata(docname, ColumnMetadata.named("DOCNAME").withIndex(13).ofType(Types.VARCHAR).withSize(100));
        addMetadata(inn, ColumnMetadata.named("INN").withIndex(2).ofType(Types.VARCHAR).withSize(100));
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(4).ofType(Types.VARCHAR).withSize(100));
        addMetadata(lastname, ColumnMetadata.named("LASTNAME").withIndex(12).ofType(Types.VARCHAR).withSize(100));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(11).ofType(Types.VARCHAR).withSize(100));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(7).ofType(Types.VARCHAR).withSize(100));
        addMetadata(orgname, ColumnMetadata.named("ORGNAME").withIndex(14).ofType(Types.VARCHAR).withSize(100));
        addMetadata(phone, ColumnMetadata.named("PHONE").withIndex(8).ofType(Types.VARCHAR).withSize(100));
        addMetadata(place, ColumnMetadata.named("PLACE").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(reorgcode, ColumnMetadata.named("REORGCODE").withIndex(15).ofType(Types.VARCHAR).withSize(100));
        addMetadata(rowNum, ColumnMetadata.named("ROW_NUM").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(sign, ColumnMetadata.named("SIGN").withIndex(9).ofType(Types.VARCHAR).withSize(100));
        addMetadata(surname, ColumnMetadata.named("SURNAME").withIndex(10).ofType(Types.VARCHAR).withSize(100));
        addMetadata(taxEnd, ColumnMetadata.named("TAX_END").withIndex(3).ofType(Types.VARCHAR).withSize(100));
        addMetadata(titname, ColumnMetadata.named("TITNAME").withIndex(6).ofType(Types.VARCHAR).withSize(255));
    }

}

