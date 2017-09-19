package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationData is a Querydsl query type for QDeclarationData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationData extends com.querydsl.sql.RelationalPathBase<QDeclarationData> {

    private static final long serialVersionUID = 1765056903;

    public static final QDeclarationData declarationData = new QDeclarationData("DECLARATION_DATA");

    public final NumberPath<Long> asnuId = createNumber("asnuId", Long.class);

    public final NumberPath<Integer> declarationTemplateId = createNumber("declarationTemplateId", Integer.class);

    public final NumberPath<Long> departmentReportPeriodId = createNumber("departmentReportPeriodId", Long.class);

    public final NumberPath<Long> docStateId = createNumber("docStateId", Long.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kpp = createString("kpp");

    public final StringPath note = createString("note");

    public final StringPath oktmo = createString("oktmo");

    public final NumberPath<Byte> state = createNumber("state", Byte.class);

    public final StringPath taxOrganCode = createString("taxOrganCode");

    public final com.querydsl.sql.PrimaryKey<QDeclarationData> declarationDataPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookDocState> declDataDocStateFk = createForeignKey(docStateId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> declarationDataFkDeclTId = createForeignKey(declarationTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> declarationDataFkAsnuId = createForeignKey(asnuId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentReportPeriod> declDataFkDepRepPerId = createForeignKey(departmentReportPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QState> declarationDataStateFk = createForeignKey(state, "ID");

    public final com.querydsl.sql.ForeignKey<QLogBusiness> _logBusinessFkDeclarationId = createInvForeignKey(id, "DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationDataConsolidation> _declDataConsolidationFkSrc = createInvForeignKey(id, "SOURCE_DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationDataConsolidation> _declDataConsolidationFkTgt = createInvForeignKey(id, "TARGET_DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QNdflPerson> _ndflPersonFkD = createInvForeignKey(id, "DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationReport> _declReportFkDeclData = createInvForeignKey(id, "DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationDataFile> _declDataFileFkDeclData = createInvForeignKey(id, "DECLARATION_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QNdflReferences> _ndflRefersDeclDataFk = createInvForeignKey(id, "DECLARATION_DATA_ID");

    public QDeclarationData(String variable) {
        super(QDeclarationData.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_DATA");
        addMetadata();
    }

    public QDeclarationData(String variable, String schema, String table) {
        super(QDeclarationData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationData(Path<? extends QDeclarationData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_DATA");
        addMetadata();
    }

    public QDeclarationData(PathMetadata metadata) {
        super(QDeclarationData.class, metadata, "NDFL_UNSTABLE", "DECLARATION_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(asnuId, ColumnMetadata.named("ASNU_ID").withIndex(7).ofType(Types.DECIMAL).withSize(18));
        addMetadata(declarationTemplateId, ColumnMetadata.named("DECLARATION_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(departmentReportPeriodId, ColumnMetadata.named("DEPARTMENT_REPORT_PERIOD_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(docStateId, ColumnMetadata.named("DOC_STATE_ID").withIndex(11).ofType(Types.DECIMAL).withSize(18));
        addMetadata(fileName, ColumnMetadata.named("FILE_NAME").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(4).ofType(Types.VARCHAR).withSize(9));
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(8).ofType(Types.VARCHAR).withSize(512));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(5).ofType(Types.VARCHAR).withSize(11));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(taxOrganCode, ColumnMetadata.named("TAX_ORGAN_CODE").withIndex(3).ofType(Types.VARCHAR).withSize(4));
    }

}

