package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QConfigurationScheduler is a Querydsl query type for QConfigurationScheduler
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QConfigurationScheduler extends com.querydsl.sql.RelationalPathBase<QConfigurationScheduler> {

    private static final long serialVersionUID = 1334083825;

    public static final QConfigurationScheduler configurationScheduler = new QConfigurationScheduler("CONFIGURATION_SCHEDULER");

    public final NumberPath<Byte> active = createNumber("active", Byte.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<org.joda.time.LocalDateTime> lastFireDate = createDateTime("lastFireDate", org.joda.time.LocalDateTime.class);

    public final DateTimePath<org.joda.time.LocalDateTime> modificationDate = createDateTime("modificationDate", org.joda.time.LocalDateTime.class);

    public final StringPath schedule = createString("schedule");

    public final StringPath taskName = createString("taskName");

    public final com.querydsl.sql.PrimaryKey<QConfigurationScheduler> confSchedulerPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QConfigurationSchedulerParam> _confSchedulerParamFkConf = createInvForeignKey(id, "TASK_ID");

    public QConfigurationScheduler(String variable) {
        super(QConfigurationScheduler.class, forVariable(variable), "NDFL_UNSTABLE", "CONFIGURATION_SCHEDULER");
        addMetadata();
    }

    public QConfigurationScheduler(String variable, String schema, String table) {
        super(QConfigurationScheduler.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QConfigurationScheduler(String variable, String schema) {
        super(QConfigurationScheduler.class, forVariable(variable), schema, "CONFIGURATION_SCHEDULER");
        addMetadata();
    }

    public QConfigurationScheduler(Path<? extends QConfigurationScheduler> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "CONFIGURATION_SCHEDULER");
        addMetadata();
    }

    public QConfigurationScheduler(PathMetadata metadata) {
        super(QConfigurationScheduler.class, metadata, "NDFL_UNSTABLE", "CONFIGURATION_SCHEDULER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(active, ColumnMetadata.named("ACTIVE").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(lastFireDate, ColumnMetadata.named("LAST_FIRE_DATE").withIndex(6).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(modificationDate, ColumnMetadata.named("MODIFICATION_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(schedule, ColumnMetadata.named("SCHEDULE").withIndex(3).ofType(Types.VARCHAR).withSize(100));
        addMetadata(taskName, ColumnMetadata.named("TASK_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
    }

}

