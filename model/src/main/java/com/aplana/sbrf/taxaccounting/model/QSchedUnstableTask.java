package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSchedUnstableTask is a Querydsl query type for QSchedUnstableTask
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchedUnstableTask extends com.querydsl.sql.RelationalPathBase<QSchedUnstableTask> {

    private static final long serialVersionUID = 2073653139;

    public static final QSchedUnstableTask schedUnstableTask = new QSchedUnstableTask("SCHED_UNSTABLE_TASK");

    public final NumberPath<Long> autopurge = createNumber("autopurge", Long.class);

    public final NumberPath<Byte> cancelled = createNumber("cancelled", Byte.class);

    public final NumberPath<java.math.BigInteger> createtime = createNumber("createtime", java.math.BigInteger.class);

    public final NumberPath<Long> failureaction = createNumber("failureaction", Long.class);

    public final NumberPath<Long> maxattempts = createNumber("maxattempts", Long.class);

    public final NumberPath<Long> maxrepeats = createNumber("maxrepeats", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> nextfiretime = createNumber("nextfiretime", java.math.BigInteger.class);

    public final StringPath ownertoken = createString("ownertoken");

    public final NumberPath<Long> partitionid = createNumber("partitionid", Long.class);

    public final NumberPath<Long> qos = createNumber("qos", Long.class);

    public final StringPath repeatinterval = createString("repeatinterval");

    public final NumberPath<Long> repeatsleft = createNumber("repeatsleft", Long.class);

    public final NumberPath<Long> rowVersion = createNumber("rowVersion", Long.class);

    public final StringPath startbyinterval = createString("startbyinterval");

    public final NumberPath<java.math.BigInteger> startbytime = createNumber("startbytime", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> taskid = createNumber("taskid", java.math.BigInteger.class);

    public final SimplePath<java.sql.Blob> taskinfo = createSimple("taskinfo", java.sql.Blob.class);

    public final NumberPath<Byte> tasksuspended = createNumber("tasksuspended", Byte.class);

    public final NumberPath<Long> tasktype = createNumber("tasktype", Long.class);

    public final NumberPath<java.math.BigInteger> validfromtime = createNumber("validfromtime", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> validtotime = createNumber("validtotime", java.math.BigInteger.class);

    public final StringPath version = createString("version");

    public final com.querydsl.sql.PrimaryKey<QSchedUnstableTask> sysC0016901 = createPrimaryKey(taskid);

    public QSchedUnstableTask(String variable) {
        super(QSchedUnstableTask.class, forVariable(variable), "NDFL_1_0", "SCHED_UNSTABLE_TASK");
        addMetadata();
    }

    public QSchedUnstableTask(String variable, String schema, String table) {
        super(QSchedUnstableTask.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchedUnstableTask(Path<? extends QSchedUnstableTask> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SCHED_UNSTABLE_TASK");
        addMetadata();
    }

    public QSchedUnstableTask(PathMetadata metadata) {
        super(QSchedUnstableTask.class, metadata, "NDFL_1_0", "SCHED_UNSTABLE_TASK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(autopurge, ColumnMetadata.named("AUTOPURGE").withIndex(17).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(cancelled, ColumnMetadata.named("CANCELLED").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(createtime, ColumnMetadata.named("CREATETIME").withIndex(23).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(failureaction, ColumnMetadata.named("FAILUREACTION").withIndex(18).ofType(Types.DECIMAL).withSize(10));
        addMetadata(maxattempts, ColumnMetadata.named("MAXATTEMPTS").withIndex(19).ofType(Types.DECIMAL).withSize(10));
        addMetadata(maxrepeats, ColumnMetadata.named("MAXREPEATS").withIndex(13).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(16).ofType(Types.VARCHAR).withSize(254));
        addMetadata(nextfiretime, ColumnMetadata.named("NEXTFIRETIME").withIndex(7).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(ownertoken, ColumnMetadata.named("OWNERTOKEN").withIndex(22).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(partitionid, ColumnMetadata.named("PARTITIONID").withIndex(21).ofType(Types.DECIMAL).withSize(10));
        addMetadata(qos, ColumnMetadata.named("QOS").withIndex(20).ofType(Types.DECIMAL).withSize(10));
        addMetadata(repeatinterval, ColumnMetadata.named("REPEATINTERVAL").withIndex(12).ofType(Types.VARCHAR).withSize(254));
        addMetadata(repeatsleft, ColumnMetadata.named("REPEATSLEFT").withIndex(14).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(rowVersion, ColumnMetadata.named("ROW_VERSION").withIndex(3).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(startbyinterval, ColumnMetadata.named("STARTBYINTERVAL").withIndex(8).ofType(Types.VARCHAR).withSize(254));
        addMetadata(startbytime, ColumnMetadata.named("STARTBYTIME").withIndex(9).ofType(Types.DECIMAL).withSize(19));
        addMetadata(taskid, ColumnMetadata.named("TASKID").withIndex(1).ofType(Types.DECIMAL).withSize(19).notNull());
        addMetadata(taskinfo, ColumnMetadata.named("TASKINFO").withIndex(15).ofType(Types.BLOB).withSize(4000));
        addMetadata(tasksuspended, ColumnMetadata.named("TASKSUSPENDED").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(tasktype, ColumnMetadata.named("TASKTYPE").withIndex(4).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(validfromtime, ColumnMetadata.named("VALIDFROMTIME").withIndex(10).ofType(Types.DECIMAL).withSize(19));
        addMetadata(validtotime, ColumnMetadata.named("VALIDTOTIME").withIndex(11).ofType(Types.DECIMAL).withSize(19));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(2).ofType(Types.VARCHAR).withSize(5).notNull());
    }

}

