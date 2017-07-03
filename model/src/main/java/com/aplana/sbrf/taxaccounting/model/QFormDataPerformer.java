package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataPerformer is a Querydsl query type for QFormDataPerformer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataPerformer extends com.querydsl.sql.RelationalPathBase<QFormDataPerformer> {

    private static final long serialVersionUID = 1847304547;

    public static final QFormDataPerformer formDataPerformer = new QFormDataPerformer("FORM_DATA_PERFORMER");

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final NumberPath<Integer> printDepartmentId = createNumber("printDepartmentId", Integer.class);

    public final StringPath reportDepartmentName = createString("reportDepartmentName");

    public final com.querydsl.sql.PrimaryKey<QFormDataPerformer> formDataPerformerPk = createPrimaryKey(formDataId);

    public final com.querydsl.sql.ForeignKey<QFormData> formdataPerformerFkFormdata = createForeignKey(formDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> formdataPerformerFkDept = createForeignKey(printDepartmentId, "ID");

    public QFormDataPerformer(String variable) {
        super(QFormDataPerformer.class, forVariable(variable), "NDFL_1_0", "FORM_DATA_PERFORMER");
        addMetadata();
    }

    public QFormDataPerformer(String variable, String schema, String table) {
        super(QFormDataPerformer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataPerformer(Path<? extends QFormDataPerformer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "FORM_DATA_PERFORMER");
        addMetadata();
    }

    public QFormDataPerformer(PathMetadata metadata) {
        super(QFormDataPerformer.class, metadata, "NDFL_1_0", "FORM_DATA_PERFORMER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(200));
        addMetadata(phone, ColumnMetadata.named("PHONE").withIndex(3).ofType(Types.VARCHAR).withSize(40));
        addMetadata(printDepartmentId, ColumnMetadata.named("PRINT_DEPARTMENT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(reportDepartmentName, ColumnMetadata.named("REPORT_DEPARTMENT_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(4000));
    }

}

