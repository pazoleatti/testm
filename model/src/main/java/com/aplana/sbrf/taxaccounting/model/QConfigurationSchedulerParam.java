package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QConfigurationSchedulerParam is a Querydsl query type for QConfigurationSchedulerParam
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QConfigurationSchedulerParam extends com.querydsl.sql.RelationalPathBase<QConfigurationSchedulerParam> {

    private static final long serialVersionUID = 1336821739;

    public static final QConfigurationSchedulerParam configurationSchedulerParam = new QConfigurationSchedulerParam("CONFIGURATION_SCHEDULER_PARAM");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final StringPath paramName = createString("paramName");

    public final NumberPath<Integer> taskId = createNumber("taskId", Integer.class);

    public final NumberPath<Byte> type = createNumber("type", Byte.class);

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<QConfigurationSchedulerParam> confSchedulerParamPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QConfigurationScheduler> confSchedulerParamFkConf = createForeignKey(taskId, "ID");

    public QConfigurationSchedulerParam(String variable) {
        super(QConfigurationSchedulerParam.class, forVariable(variable), "NDFL_1_0", "CONFIGURATION_SCHEDULER_PARAM");
        addMetadata();
    }

    public QConfigurationSchedulerParam(String variable, String schema, String table) {
        super(QConfigurationSchedulerParam.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QConfigurationSchedulerParam(Path<? extends QConfigurationSchedulerParam> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "CONFIGURATION_SCHEDULER_PARAM");
        addMetadata();
    }

    public QConfigurationSchedulerParam(PathMetadata metadata) {
        super(QConfigurationSchedulerParam.class, metadata, "NDFL_1_0", "CONFIGURATION_SCHEDULER_PARAM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(paramName, ColumnMetadata.named("PARAM_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(taskId, ColumnMetadata.named("TASK_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(6).ofType(Types.VARCHAR).withSize(200).notNull());
    }

}

