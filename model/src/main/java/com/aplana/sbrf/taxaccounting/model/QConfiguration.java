package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QConfiguration is a Querydsl query type for QConfiguration
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QConfiguration extends com.querydsl.sql.RelationalPathBase<QConfiguration> {

    private static final long serialVersionUID = 1471545081;

    public static final QConfiguration configuration = new QConfiguration("CONFIGURATION");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<QConfiguration> configurationPk = createPrimaryKey(code, departmentId);

    public final com.querydsl.sql.ForeignKey<QDepartment> configurationFk = createForeignKey(departmentId, "ID");

    public QConfiguration(String variable) {
        super(QConfiguration.class, forVariable(variable), "NDFL_UNSTABLE", "CONFIGURATION");
        addMetadata();
    }

    public QConfiguration(String variable, String schema, String table) {
        super(QConfiguration.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QConfiguration(Path<? extends QConfiguration> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "CONFIGURATION");
        addMetadata();
    }

    public QConfiguration(PathMetadata metadata) {
        super(QConfiguration.class, metadata, "NDFL_UNSTABLE", "CONFIGURATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(1).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(2048));
    }

}

