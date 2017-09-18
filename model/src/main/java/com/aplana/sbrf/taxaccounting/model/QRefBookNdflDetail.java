package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookNdflDetail is a Querydsl query type for QRefBookNdflDetail
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookNdflDetail extends com.querydsl.sql.RelationalPathBase<QRefBookNdflDetail> {

    private static final long serialVersionUID = -481002452;

    public static final QRefBookNdflDetail refBookNdflDetail = new QRefBookNdflDetail("REF_BOOK_NDFL_DETAIL");

    public final StringPath approveDocName = createString("approveDocName");

    public final StringPath approveOrgName = createString("approveOrgName");

    public final NumberPath<Long> departmentId = createNumber("departmentId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kpp = createString("kpp");

    public final StringPath name = createString("name");

    public final NumberPath<Long> obligation = createNumber("obligation", Long.class);

    public final NumberPath<Long> oktmo = createNumber("oktmo", Long.class);

    public final NumberPath<Long> okved = createNumber("okved", Long.class);

    public final StringPath phone = createString("phone");

    public final NumberPath<Long> presentPlace = createNumber("presentPlace", Long.class);

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Long> refBookNdflId = createNumber("refBookNdflId", Long.class);

    public final NumberPath<Long> region = createNumber("region", Long.class);

    public final NumberPath<Long> reorgFormCode = createNumber("reorgFormCode", Long.class);

    public final StringPath reorgInn = createString("reorgInn");

    public final StringPath reorgKpp = createString("reorgKpp");

    public final NumberPath<Short> rowOrd = createNumber("rowOrd", Short.class);

    public final StringPath signatoryFirstname = createString("signatoryFirstname");

    public final NumberPath<Long> signatoryId = createNumber("signatoryId", Long.class);

    public final StringPath signatoryLastname = createString("signatoryLastname");

    public final StringPath signatorySurname = createString("signatorySurname");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final StringPath taxOrganCode = createString("taxOrganCode");

    public final StringPath taxOrganCodeMid = createString("taxOrganCodeMid");

    public final NumberPath<Long> type = createNumber("type", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookNdflDetail> refBookNdflDetailPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookReorganization> refBookNdflDetReCodeFk = createForeignKey(reorgFormCode, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookNdfl> refBookNdflDetParentFk = createForeignKey(refBookNdflId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookOktmo> refBookNdflDetOktmoFk = createForeignKey(oktmo, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookRegion> refBookNdflDetRegionFk = createForeignKey(region, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookDetachTaxPay> refBookNdflDetObligFk = createForeignKey(obligation, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookPresentPlace> refBookNdflDetPresPlFk = createForeignKey(presentPlace, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookOkved> refBookNdflDetOkvedFk = createForeignKey(okved, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookSignatoryMark> refBookNdflDetSignatoryFk = createForeignKey(signatoryId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> refBookNdflDetDepartFk = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookMakeCalc> refBookNdflDetTypeFk = createForeignKey(type, "ID");

    public QRefBookNdflDetail(String variable) {
        super(QRefBookNdflDetail.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_NDFL_DETAIL");
        addMetadata();
    }

    public QRefBookNdflDetail(String variable, String schema, String table) {
        super(QRefBookNdflDetail.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookNdflDetail(Path<? extends QRefBookNdflDetail> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_NDFL_DETAIL");
        addMetadata();
    }

    public QRefBookNdflDetail(PathMetadata metadata) {
        super(QRefBookNdflDetail.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_NDFL_DETAIL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(approveDocName, ColumnMetadata.named("APPROVE_DOC_NAME").withIndex(26).ofType(Types.VARCHAR).withSize(120));
        addMetadata(approveOrgName, ColumnMetadata.named("APPROVE_ORG_NAME").withIndex(27).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(7).ofType(Types.DECIMAL).withSize(18));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(9).ofType(Types.VARCHAR).withSize(9));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(12).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(obligation, ColumnMetadata.named("OBLIGATION").withIndex(17).ofType(Types.DECIMAL).withSize(18));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(15).ofType(Types.DECIMAL).withSize(18));
        addMetadata(okved, ColumnMetadata.named("OKVED").withIndex(13).ofType(Types.DECIMAL).withSize(18));
        addMetadata(phone, ColumnMetadata.named("PHONE").withIndex(16).ofType(Types.VARCHAR).withSize(25));
        addMetadata(presentPlace, ColumnMetadata.named("PRESENT_PLACE").withIndex(11).ofType(Types.DECIMAL).withSize(18));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(refBookNdflId, ColumnMetadata.named("REF_BOOK_NDFL_ID").withIndex(5).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(region, ColumnMetadata.named("REGION").withIndex(14).ofType(Types.DECIMAL).withSize(18));
        addMetadata(reorgFormCode, ColumnMetadata.named("REORG_FORM_CODE").withIndex(19).ofType(Types.DECIMAL).withSize(18));
        addMetadata(reorgInn, ColumnMetadata.named("REORG_INN").withIndex(20).ofType(Types.VARCHAR).withSize(12));
        addMetadata(reorgKpp, ColumnMetadata.named("REORG_KPP").withIndex(21).ofType(Types.VARCHAR).withSize(9));
        addMetadata(rowOrd, ColumnMetadata.named("ROW_ORD").withIndex(6).ofType(Types.DECIMAL).withSize(4).notNull());
        addMetadata(signatoryFirstname, ColumnMetadata.named("SIGNATORY_FIRSTNAME").withIndex(24).ofType(Types.VARCHAR).withSize(60));
        addMetadata(signatoryId, ColumnMetadata.named("SIGNATORY_ID").withIndex(22).ofType(Types.DECIMAL).withSize(18));
        addMetadata(signatoryLastname, ColumnMetadata.named("SIGNATORY_LASTNAME").withIndex(25).ofType(Types.VARCHAR).withSize(60));
        addMetadata(signatorySurname, ColumnMetadata.named("SIGNATORY_SURNAME").withIndex(23).ofType(Types.VARCHAR).withSize(60));
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(taxOrganCode, ColumnMetadata.named("TAX_ORGAN_CODE").withIndex(8).ofType(Types.VARCHAR).withSize(4));
        addMetadata(taxOrganCodeMid, ColumnMetadata.named("TAX_ORGAN_CODE_MID").withIndex(10).ofType(Types.VARCHAR).withSize(4));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(18).ofType(Types.DECIMAL).withSize(18));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

