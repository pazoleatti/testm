package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationSource is a Querydsl query type for QDeclarationSource
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationSource extends com.querydsl.sql.RelationalPathBase<QDeclarationSource> {

    private static final long serialVersionUID = 150017720;

    public static final QDeclarationSource declarationSource = new QDeclarationSource("DECLARATION_SOURCE");

    public final NumberPath<Integer> departmentDeclarationTypeId = createNumber("departmentDeclarationTypeId", Integer.class);

    public final DateTimePath<org.joda.time.LocalDateTime> periodEnd = createDateTime("periodEnd", org.joda.time.LocalDateTime.class);

    public final DateTimePath<org.joda.time.LocalDateTime> periodStart = createDateTime("periodStart", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> srcDepartmentFormTypeId = createNumber("srcDepartmentFormTypeId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDeclarationSource> declarationSourcePk = createPrimaryKey(departmentDeclarationTypeId, periodStart, srcDepartmentFormTypeId);

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclarationType> declSourceFkDeptDecltype = createForeignKey(departmentDeclarationTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> declSourceFkDeptFormtype = createForeignKey(srcDepartmentFormTypeId, "ID");

    public QDeclarationSource(String variable) {
        super(QDeclarationSource.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_SOURCE");
        addMetadata();
    }

    public QDeclarationSource(String variable, String schema, String table) {
        super(QDeclarationSource.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationSource(Path<? extends QDeclarationSource> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_SOURCE");
        addMetadata();
    }

    public QDeclarationSource(PathMetadata metadata) {
        super(QDeclarationSource.class, metadata, "NDFL_UNSTABLE", "DECLARATION_SOURCE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentDeclarationTypeId, ColumnMetadata.named("DEPARTMENT_DECLARATION_TYPE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(periodEnd, ColumnMetadata.named("PERIOD_END").withIndex(4).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(periodStart, ColumnMetadata.named("PERIOD_START").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(srcDepartmentFormTypeId, ColumnMetadata.named("SRC_DEPARTMENT_FORM_TYPE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

