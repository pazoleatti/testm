package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataSource is a Querydsl query type for QFormDataSource
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataSource extends com.querydsl.sql.RelationalPathBase<QFormDataSource> {

    private static final long serialVersionUID = -1440557402;

    public static final QFormDataSource formDataSource = new QFormDataSource("FORM_DATA_SOURCE");

    public final NumberPath<Integer> departmentFormTypeId = createNumber("departmentFormTypeId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> periodEnd = createDateTime("periodEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> periodStart = createDateTime("periodStart", java.sql.Timestamp.class);

    public final NumberPath<Integer> srcDepartmentFormTypeId = createNumber("srcDepartmentFormTypeId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QFormDataSource> formDataSourcePk = createPrimaryKey(departmentFormTypeId, periodStart, srcDepartmentFormTypeId);

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> formDataSourceFkSrcDepId = createForeignKey(srcDepartmentFormTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> formDataSourceFkDepId = createForeignKey(departmentFormTypeId, "ID");

    public QFormDataSource(String variable) {
        super(QFormDataSource.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_DATA_SOURCE");
        addMetadata();
    }

    public QFormDataSource(String variable, String schema, String table) {
        super(QFormDataSource.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataSource(Path<? extends QFormDataSource> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_DATA_SOURCE");
        addMetadata();
    }

    public QFormDataSource(PathMetadata metadata) {
        super(QFormDataSource.class, metadata, "NDFL_UNSTABLE", "FORM_DATA_SOURCE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentFormTypeId, ColumnMetadata.named("DEPARTMENT_FORM_TYPE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(periodEnd, ColumnMetadata.named("PERIOD_END").withIndex(4).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(periodStart, ColumnMetadata.named("PERIOD_START").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(srcDepartmentFormTypeId, ColumnMetadata.named("SRC_DEPARTMENT_FORM_TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

