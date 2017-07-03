package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDataPumpXplTable$ is a Querydsl query type for QDataPumpXplTable$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDataPumpXplTable$ extends com.querydsl.sql.RelationalPathBase<QDataPumpXplTable$> {

    private static final long serialVersionUID = 1444433691;

    public static final QDataPumpXplTable$ dataPumpXplTable$ = new QDataPumpXplTable$("DATA_PUMP_XPL_TABLE$");

    public final StringPath accessPredicates = createString("accessPredicates");

    public final NumberPath<java.math.BigInteger> bytes = createNumber("bytes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cardinality = createNumber("cardinality", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cost = createNumber("cost", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cpuCost = createNumber("cpuCost", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> depth = createNumber("depth", java.math.BigInteger.class);

    public final StringPath distribution = createString("distribution");

    public final StringPath filterPredicates = createString("filterPredicates");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ioCost = createNumber("ioCost", java.math.BigInteger.class);

    public final StringPath objectAlias = createString("objectAlias");

    public final NumberPath<java.math.BigInteger> objectInstance = createNumber("objectInstance", java.math.BigInteger.class);

    public final StringPath objectName = createString("objectName");

    public final StringPath objectNode = createString("objectNode");

    public final StringPath objectOwner = createString("objectOwner");

    public final StringPath objectType = createString("objectType");

    public final StringPath operation = createString("operation");

    public final StringPath optimizer = createString("optimizer");

    public final StringPath options = createString("options");

    public final StringPath other = createString("other");

    public final StringPath otherTag = createString("otherTag");

    public final StringPath otherXml = createString("otherXml");

    public final NumberPath<java.math.BigInteger> parentId = createNumber("parentId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> partitionId = createNumber("partitionId", java.math.BigInteger.class);

    public final StringPath partitionStart = createString("partitionStart");

    public final StringPath partitionStop = createString("partitionStop");

    public final NumberPath<java.math.BigInteger> planId = createNumber("planId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> position = createNumber("position", java.math.BigInteger.class);

    public final StringPath projection = createString("projection");

    public final StringPath qblockName = createString("qblockName");

    public final StringPath remarks = createString("remarks");

    public final NumberPath<java.math.BigInteger> searchColumns = createNumber("searchColumns", java.math.BigInteger.class);

    public final StringPath statementId = createString("statementId");

    public final NumberPath<java.math.BigInteger> tempSpace = createNumber("tempSpace", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> time = createNumber("time", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> timestamp = createDateTime("timestamp", org.joda.time.DateTime.class);

    public QDataPumpXplTable$(String variable) {
        super(QDataPumpXplTable$.class, forVariable(variable), "SYS", "DATA_PUMP_XPL_TABLE$");
        addMetadata();
    }

    public QDataPumpXplTable$(String variable, String schema, String table) {
        super(QDataPumpXplTable$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDataPumpXplTable$(Path<? extends QDataPumpXplTable$> path) {
        super(path.getType(), path.getMetadata(), "SYS", "DATA_PUMP_XPL_TABLE$");
        addMetadata();
    }

    public QDataPumpXplTable$(PathMetadata metadata) {
        super(QDataPumpXplTable$.class, metadata, "SYS", "DATA_PUMP_XPL_TABLE$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(accessPredicates, ColumnMetadata.named("ACCESS_PREDICATES").withIndex(31).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(bytes, ColumnMetadata.named("BYTES").withIndex(21).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cardinality, ColumnMetadata.named("CARDINALITY").withIndex(20).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cost, ColumnMetadata.named("COST").withIndex(19).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cpuCost, ColumnMetadata.named("CPU_COST").withIndex(28).ofType(Types.DECIMAL).withSize(22));
        addMetadata(depth, ColumnMetadata.named("DEPTH").withIndex(17).ofType(Types.DECIMAL).withSize(22));
        addMetadata(distribution, ColumnMetadata.named("DISTRIBUTION").withIndex(27).ofType(Types.VARCHAR).withSize(30));
        addMetadata(filterPredicates, ColumnMetadata.named("FILTER_PREDICATES").withIndex(32).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(15).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ioCost, ColumnMetadata.named("IO_COST").withIndex(29).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectAlias, ColumnMetadata.named("OBJECT_ALIAS").withIndex(10).ofType(Types.VARCHAR).withSize(65));
        addMetadata(objectInstance, ColumnMetadata.named("OBJECT_INSTANCE").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectName, ColumnMetadata.named("OBJECT_NAME").withIndex(9).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectNode, ColumnMetadata.named("OBJECT_NODE").withIndex(7).ofType(Types.VARCHAR).withSize(128));
        addMetadata(objectOwner, ColumnMetadata.named("OBJECT_OWNER").withIndex(8).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectType, ColumnMetadata.named("OBJECT_TYPE").withIndex(12).ofType(Types.VARCHAR).withSize(30));
        addMetadata(operation, ColumnMetadata.named("OPERATION").withIndex(5).ofType(Types.VARCHAR).withSize(30));
        addMetadata(optimizer, ColumnMetadata.named("OPTIMIZER").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(other, ColumnMetadata.named("OTHER").withIndex(26).ofType(Types.LONGVARCHAR).withSize(0));
        addMetadata(otherTag, ColumnMetadata.named("OTHER_TAG").withIndex(22).ofType(Types.VARCHAR).withSize(255));
        addMetadata(otherXml, ColumnMetadata.named("OTHER_XML").withIndex(36).ofType(Types.CLOB).withSize(4000));
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(partitionId, ColumnMetadata.named("PARTITION_ID").withIndex(25).ofType(Types.DECIMAL).withSize(22));
        addMetadata(partitionStart, ColumnMetadata.named("PARTITION_START").withIndex(23).ofType(Types.VARCHAR).withSize(255));
        addMetadata(partitionStop, ColumnMetadata.named("PARTITION_STOP").withIndex(24).ofType(Types.VARCHAR).withSize(255));
        addMetadata(planId, ColumnMetadata.named("PLAN_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(position, ColumnMetadata.named("POSITION").withIndex(18).ofType(Types.DECIMAL).withSize(22));
        addMetadata(projection, ColumnMetadata.named("PROJECTION").withIndex(33).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(qblockName, ColumnMetadata.named("QBLOCK_NAME").withIndex(35).ofType(Types.VARCHAR).withSize(30));
        addMetadata(remarks, ColumnMetadata.named("REMARKS").withIndex(4).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(searchColumns, ColumnMetadata.named("SEARCH_COLUMNS").withIndex(14).ofType(Types.DECIMAL).withSize(22));
        addMetadata(statementId, ColumnMetadata.named("STATEMENT_ID").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(tempSpace, ColumnMetadata.named("TEMP_SPACE").withIndex(30).ofType(Types.DECIMAL).withSize(22));
        addMetadata(time, ColumnMetadata.named("TIME").withIndex(34).ofType(Types.DECIMAL).withSize(22));
        addMetadata(timestamp, ColumnMetadata.named("TIMESTAMP").withIndex(3).ofType(Types.TIMESTAMP).withSize(7));
    }

}

