package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormData is a Querydsl query type for QFormData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormData extends com.querydsl.sql.RelationalPathBase<QFormData> {

    private static final long serialVersionUID = 493776555;

    public static final QFormData formData = new QFormData("FORM_DATA");

    public final NumberPath<Byte> accruing = createNumber("accruing", Byte.class);

    public final NumberPath<Long> comparativeDepRepPerId = createNumber("comparativeDepRepPerId", Long.class);

    public final NumberPath<Long> departmentReportPeriodId = createNumber("departmentReportPeriodId", Long.class);

    public final NumberPath<Byte> edited = createNumber("edited", Byte.class);

    public final NumberPath<Integer> formTemplateId = createNumber("formTemplateId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> kind = createNumber("kind", Integer.class);

    public final NumberPath<Byte> manual = createNumber("manual", Byte.class);

    public final StringPath note = createString("note");

    public final NumberPath<Integer> numberCurrentRow = createNumber("numberCurrentRow", Integer.class);

    public final NumberPath<Integer> numberPreviousRow = createNumber("numberPreviousRow", Integer.class);

    public final NumberPath<Byte> periodOrder = createNumber("periodOrder", Byte.class);

    public final NumberPath<Byte> returnSign = createNumber("returnSign", Byte.class);

    public final NumberPath<Byte> sorted = createNumber("sorted", Byte.class);

    public final NumberPath<Byte> sortedBackup = createNumber("sortedBackup", Byte.class);

    public final NumberPath<Integer> state = createNumber("state", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QFormData> formDataPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormKind> formDataFkKind = createForeignKey(kind, "ID");

    public final com.querydsl.sql.ForeignKey<QFormTemplate> formDataFkFormTemplId = createForeignKey(formTemplateId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentReportPeriod> formDataFkDepRepPerId = createForeignKey(departmentReportPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentReportPeriod> formDataFkCoDepRepPerId = createForeignKey(comparativeDepRepPerId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormDataSigner> _formDataSignerFkFormdata = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataReport> _formDataRepFkFormDataId = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataPerformer> _formdataPerformerFkFormdata = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataRefBook> _formDataRefBookFkFormdata = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QLogBusiness> _logBusinessFkFormDataId = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataFile> _formDataFileFkFormData = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataConsolidation> _formDataConsolidationFkTgt = createInvForeignKey(id, "TARGET_FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormSearchResult> _formSearchResultFkFormdata = createInvForeignKey(id, "FORM_DATA_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataConsolidation> _formDataConsolidationFkSrc = createInvForeignKey(id, "SOURCE_FORM_DATA_ID");

    public QFormData(String variable) {
        super(QFormData.class, forVariable(variable), "NDFL_1_0", "FORM_DATA");
        addMetadata();
    }

    public QFormData(String variable, String schema, String table) {
        super(QFormData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormData(Path<? extends QFormData> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_DATA");
        addMetadata();
    }

    public QFormData(PathMetadata metadata) {
        super(QFormData.class, metadata, "NDFL_1_0", "FORM_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accruing, ColumnMetadata.named("ACCRUING").withIndex(13).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(comparativeDepRepPerId, ColumnMetadata.named("COMPARATIVE_DEP_REP_PER_ID").withIndex(12).ofType(Types.DECIMAL).withSize(18));
        addMetadata(departmentReportPeriodId, ColumnMetadata.named("DEPARTMENT_REPORT_PERIOD_ID").withIndex(8).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(edited, ColumnMetadata.named("EDITED").withIndex(15).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(formTemplateId, ColumnMetadata.named("FORM_TEMPLATE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kind, ColumnMetadata.named("KIND").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(manual, ColumnMetadata.named("MANUAL").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(16).ofType(Types.VARCHAR).withSize(512));
        addMetadata(numberCurrentRow, ColumnMetadata.named("NUMBER_CURRENT_ROW").withIndex(11).ofType(Types.DECIMAL).withSize(9));
        addMetadata(numberPreviousRow, ColumnMetadata.named("NUMBER_PREVIOUS_ROW").withIndex(7).ofType(Types.DECIMAL).withSize(9));
        addMetadata(periodOrder, ColumnMetadata.named("PERIOD_ORDER").withIndex(6).ofType(Types.DECIMAL).withSize(2));
        addMetadata(returnSign, ColumnMetadata.named("RETURN_SIGN").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(sorted, ColumnMetadata.named("SORTED").withIndex(10).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(sortedBackup, ColumnMetadata.named("SORTED_BACKUP").withIndex(14).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

