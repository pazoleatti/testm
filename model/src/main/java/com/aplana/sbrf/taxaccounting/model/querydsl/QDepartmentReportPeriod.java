package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentReportPeriod is a Querydsl query type for QDepartmentReportPeriod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentReportPeriod extends com.querydsl.sql.RelationalPathBase<QDepartmentReportPeriod> {

    private static final long serialVersionUID = 196021523;

    public static final QDepartmentReportPeriod departmentReportPeriod = new QDepartmentReportPeriod("DEPARTMENT_REPORT_PERIOD");

    public final DateTimePath<org.joda.time.LocalDateTime> correctionDate = createDateTime("correctionDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> isActive = createNumber("isActive", Byte.class);

    public final NumberPath<Integer> reportPeriodId = createNumber("reportPeriodId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentReportPeriod> departmentReportPeriodPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QReportPeriod> depRepPerFkRepPeriodId = createForeignKey(reportPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> depRepPerFkDepartmentId = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> _declDataFkDepRepPerId = createInvForeignKey(id, "DEPARTMENT_REPORT_PERIOD_ID");

    public QDepartmentReportPeriod(String variable) {
        super(QDepartmentReportPeriod.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_REPORT_PERIOD");
        addMetadata();
    }

    public QDepartmentReportPeriod(String variable, String schema, String table) {
        super(QDepartmentReportPeriod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentReportPeriod(String variable, String schema) {
        super(QDepartmentReportPeriod.class, forVariable(variable), schema, "DEPARTMENT_REPORT_PERIOD");
        addMetadata();
    }

    public QDepartmentReportPeriod(Path<? extends QDepartmentReportPeriod> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_REPORT_PERIOD");
        addMetadata();
    }

    public QDepartmentReportPeriod(PathMetadata metadata) {
        super(QDepartmentReportPeriod.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_REPORT_PERIOD");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(correctionDate, ColumnMetadata.named("CORRECTION_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(isActive, ColumnMetadata.named("IS_ACTIVE").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(reportPeriodId, ColumnMetadata.named("REPORT_PERIOD_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

