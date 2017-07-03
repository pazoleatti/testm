package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QKu$DatapumpMaster11107 is a Querydsl query type for QKu$DatapumpMaster11107
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QKu$DatapumpMaster11107 extends com.querydsl.sql.RelationalPathBase<QKu$DatapumpMaster11107> {

    private static final long serialVersionUID = 1364435271;

    public static final QKu$DatapumpMaster11107 ku$DatapumpMaster11107 = new QKu$DatapumpMaster11107("KU$_DATAPUMP_MASTER_11_1_0_7");

    public final NumberPath<java.math.BigInteger> abortStep = createNumber("abortStep", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ancestorProcessOrder = createNumber("ancestorProcessOrder", java.math.BigInteger.class);

    public final StringPath baseObjectName = createString("baseObjectName");

    public final StringPath baseObjectSchema = createString("baseObjectSchema");

    public final StringPath baseObjectType = createString("baseObjectType");

    public final NumberPath<java.math.BigInteger> baseProcessOrder = createNumber("baseProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> blockSize = createNumber("blockSize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> clusterOk = createNumber("clusterOk", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> completedBytes = createNumber("completedBytes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> completedRows = createNumber("completedRows", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> completionTime = createDateTime("completionTime", org.joda.time.DateTime.class);

    public final StringPath controlQueue = createString("controlQueue");

    public final NumberPath<java.math.BigInteger> creationLevel = createNumber("creationLevel", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> cumulativeTime = createNumber("cumulativeTime", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dataBufferSize = createNumber("dataBufferSize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dataIo = createNumber("dataIo", java.math.BigInteger.class);

    public final StringPath dbVersion = createString("dbVersion");

    public final NumberPath<java.math.BigInteger> degree = createNumber("degree", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> domainProcessOrder = createNumber("domainProcessOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dumpAllocation = createNumber("dumpAllocation", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dumpFileid = createNumber("dumpFileid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dumpLength = createNumber("dumpLength", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dumpOrigLength = createNumber("dumpOrigLength", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> dumpPosition = createNumber("dumpPosition", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> duplicate = createNumber("duplicate", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> elapsedTime = createNumber("elapsedTime", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> errorCount = createNumber("errorCount", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> extendSize = createNumber("extendSize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> fileMaxSize = createNumber("fileMaxSize", java.math.BigInteger.class);

    public final StringPath fileName = createString("fileName");

    public final NumberPath<java.math.BigInteger> fileType = createNumber("fileType", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> flags = createNumber("flags", java.math.BigInteger.class);

    public final StringPath grantor = createString("grantor");

    public final NumberPath<java.math.BigInteger> granules = createNumber("granules", java.math.BigInteger.class);

    public final SimplePath<byte[]> guid = createSimple("guid", byte[].class);

    public final StringPath inProgress = createString("inProgress");

    public final StringPath instance = createString("instance");

    public final NumberPath<java.math.BigInteger> isDefault = createNumber("isDefault", java.math.BigInteger.class);

    public final StringPath jobMode = createString("jobMode");

    public final StringPath jobVersion = createString("jobVersion");

    public final NumberPath<java.math.BigInteger> lastFile = createNumber("lastFile", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> lastUpdate = createDateTime("lastUpdate", org.joda.time.DateTime.class);

    public final NumberPath<java.math.BigInteger> loadMethod = createNumber("loadMethod", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> metadataBufferSize = createNumber("metadataBufferSize", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> metadataIo = createNumber("metadataIo", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public final StringPath objectLongName = createString("objectLongName");

    public final StringPath objectName = createString("objectName");

    public final NumberPath<java.math.BigInteger> objectNumber = createNumber("objectNumber", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> objectPathSeqno = createNumber("objectPathSeqno", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> objectRow = createNumber("objectRow", java.math.BigInteger.class);

    public final StringPath objectSchema = createString("objectSchema");

    public final StringPath objectTablespace = createString("objectTablespace");

    public final StringPath objectType = createString("objectType");

    public final StringPath objectTypePath = createString("objectTypePath");

    public final StringPath oldValue = createString("oldValue");

    public final StringPath operation = createString("operation");

    public final StringPath originalObjectName = createString("originalObjectName");

    public final StringPath originalObjectSchema = createString("originalObjectSchema");

    public final NumberPath<java.math.BigInteger> packetNumber = createNumber("packetNumber", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> parallelization = createNumber("parallelization", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> parentProcessOrder = createNumber("parentProcessOrder", java.math.BigInteger.class);

    public final StringPath partitionName = createString("partitionName");

    public final NumberPath<java.math.BigInteger> phase = createNumber("phase", java.math.BigInteger.class);

    public final StringPath platform = createString("platform");

    public final StringPath processingState = createString("processingState");

    public final StringPath processingStatus = createString("processingStatus");

    public final StringPath processName = createString("processName");

    public final NumberPath<java.math.BigInteger> processOrder = createNumber("processOrder", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> property = createNumber("property", java.math.BigInteger.class);

    public final StringPath remoteLink = createString("remoteLink");

    public final NumberPath<java.math.BigInteger> scn = createNumber("scn", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> seed = createNumber("seed", java.math.BigInteger.class);

    public final StringPath serviceName = createString("serviceName");

    public final NumberPath<java.math.BigInteger> sizeEstimate = createNumber("sizeEstimate", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> startTime = createDateTime("startTime", org.joda.time.DateTime.class);

    public final StringPath state = createString("state");

    public final StringPath statusQueue = createString("statusQueue");

    public final StringPath subpartitionName = createString("subpartitionName");

    public final StringPath timezone = createString("timezone");

    public final NumberPath<java.math.BigInteger> totalBytes = createNumber("totalBytes", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> trigflag = createNumber("trigflag", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> unloadMethod = createNumber("unloadMethod", java.math.BigInteger.class);

    public final StringPath userDirectory = createString("userDirectory");

    public final StringPath userFileName = createString("userFileName");

    public final StringPath userName = createString("userName");

    public final NumberPath<java.math.BigInteger> valueN = createNumber("valueN", java.math.BigInteger.class);

    public final StringPath valueT = createString("valueT");

    public final NumberPath<java.math.BigInteger> version = createNumber("version", java.math.BigInteger.class);

    public final StringPath workItem = createString("workItem");

    public final StringPath xmlClob = createString("xmlClob");

    public QKu$DatapumpMaster11107(String variable) {
        super(QKu$DatapumpMaster11107.class, forVariable(variable), "SYS", "KU$_DATAPUMP_MASTER_11_1_0_7");
        addMetadata();
    }

    public QKu$DatapumpMaster11107(String variable, String schema, String table) {
        super(QKu$DatapumpMaster11107.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QKu$DatapumpMaster11107(Path<? extends QKu$DatapumpMaster11107> path) {
        super(path.getType(), path.getMetadata(), "SYS", "KU$_DATAPUMP_MASTER_11_1_0_7");
        addMetadata();
    }

    public QKu$DatapumpMaster11107(PathMetadata metadata) {
        super(QKu$DatapumpMaster11107.class, metadata, "SYS", "KU$_DATAPUMP_MASTER_11_1_0_7");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(abortStep, ColumnMetadata.named("ABORT_STEP").withIndex(88).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ancestorProcessOrder, ColumnMetadata.named("ANCESTOR_PROCESS_ORDER").withIndex(36).ofType(Types.DECIMAL).withSize(22));
        addMetadata(baseObjectName, ColumnMetadata.named("BASE_OBJECT_NAME").withIndex(34).ofType(Types.VARCHAR).withSize(30));
        addMetadata(baseObjectSchema, ColumnMetadata.named("BASE_OBJECT_SCHEMA").withIndex(35).ofType(Types.VARCHAR).withSize(30));
        addMetadata(baseObjectType, ColumnMetadata.named("BASE_OBJECT_TYPE").withIndex(33).ofType(Types.VARCHAR).withSize(30));
        addMetadata(baseProcessOrder, ColumnMetadata.named("BASE_PROCESS_ORDER").withIndex(32).ofType(Types.DECIMAL).withSize(22));
        addMetadata(blockSize, ColumnMetadata.named("BLOCK_SIZE").withIndex(83).ofType(Types.DECIMAL).withSize(22));
        addMetadata(clusterOk, ColumnMetadata.named("CLUSTER_OK").withIndex(90).ofType(Types.DECIMAL).withSize(22));
        addMetadata(completedBytes, ColumnMetadata.named("COMPLETED_BYTES").withIndex(60).ofType(Types.DECIMAL).withSize(22));
        addMetadata(completedRows, ColumnMetadata.named("COMPLETED_ROWS").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(completionTime, ColumnMetadata.named("COMPLETION_TIME").withIndex(26).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(controlQueue, ColumnMetadata.named("CONTROL_QUEUE").withIndex(72).ofType(Types.VARCHAR).withSize(30));
        addMetadata(creationLevel, ColumnMetadata.named("CREATION_LEVEL").withIndex(25).ofType(Types.DECIMAL).withSize(22));
        addMetadata(cumulativeTime, ColumnMetadata.named("CUMULATIVE_TIME").withIndex(64).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dataBufferSize, ColumnMetadata.named("DATA_BUFFER_SIZE").withIndex(85).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dataIo, ColumnMetadata.named("DATA_IO").withIndex(63).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dbVersion, ColumnMetadata.named("DB_VERSION").withIndex(77).ofType(Types.VARCHAR).withSize(30));
        addMetadata(degree, ColumnMetadata.named("DEGREE").withIndex(86).ofType(Types.DECIMAL).withSize(22));
        addMetadata(domainProcessOrder, ColumnMetadata.named("DOMAIN_PROCESS_ORDER").withIndex(37).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dumpAllocation, ColumnMetadata.named("DUMP_ALLOCATION").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dumpFileid, ColumnMetadata.named("DUMP_FILEID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dumpLength, ColumnMetadata.named("DUMP_LENGTH").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dumpOrigLength, ColumnMetadata.named("DUMP_ORIG_LENGTH").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(dumpPosition, ColumnMetadata.named("DUMP_POSITION").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(duplicate, ColumnMetadata.named("DUPLICATE").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(elapsedTime, ColumnMetadata.named("ELAPSED_TIME").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(errorCount, ColumnMetadata.named("ERROR_COUNT").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(extendSize, ColumnMetadata.named("EXTEND_SIZE").withIndex(54).ofType(Types.DECIMAL).withSize(22));
        addMetadata(fileMaxSize, ColumnMetadata.named("FILE_MAX_SIZE").withIndex(55).ofType(Types.DECIMAL).withSize(22));
        addMetadata(fileName, ColumnMetadata.named("FILE_NAME").withIndex(53).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(fileType, ColumnMetadata.named("FILE_TYPE").withIndex(50).ofType(Types.DECIMAL).withSize(22));
        addMetadata(flags, ColumnMetadata.named("FLAGS").withIndex(22).ofType(Types.DECIMAL).withSize(22));
        addMetadata(grantor, ColumnMetadata.named("GRANTOR").withIndex(43).ofType(Types.VARCHAR).withSize(30));
        addMetadata(granules, ColumnMetadata.named("GRANULES").withIndex(41).ofType(Types.DECIMAL).withSize(22));
        addMetadata(guid, ColumnMetadata.named("GUID").withIndex(81).ofType(Types.VARBINARY).withSize(16));
        addMetadata(inProgress, ColumnMetadata.named("IN_PROGRESS").withIndex(14).ofType(Types.CHAR).withSize(1));
        addMetadata(instance, ColumnMetadata.named("INSTANCE").withIndex(89).ofType(Types.VARCHAR).withSize(60));
        addMetadata(isDefault, ColumnMetadata.named("IS_DEFAULT").withIndex(49).ofType(Types.DECIMAL).withSize(22));
        addMetadata(jobMode, ColumnMetadata.named("JOB_MODE").withIndex(71).ofType(Types.VARCHAR).withSize(30));
        addMetadata(jobVersion, ColumnMetadata.named("JOB_VERSION").withIndex(76).ofType(Types.VARCHAR).withSize(30));
        addMetadata(lastFile, ColumnMetadata.named("LAST_FILE").withIndex(68).ofType(Types.DECIMAL).withSize(22));
        addMetadata(lastUpdate, ColumnMetadata.named("LAST_UPDATE").withIndex(57).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(loadMethod, ColumnMetadata.named("LOAD_METHOD").withIndex(40).ofType(Types.DECIMAL).withSize(22));
        addMetadata(metadataBufferSize, ColumnMetadata.named("METADATA_BUFFER_SIZE").withIndex(84).ofType(Types.DECIMAL).withSize(22));
        addMetadata(metadataIo, ColumnMetadata.named("METADATA_IO").withIndex(62).ofType(Types.DECIMAL).withSize(22));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(46).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectLongName, ColumnMetadata.named("OBJECT_LONG_NAME").withIndex(16).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(objectName, ColumnMetadata.named("OBJECT_NAME").withIndex(15).ofType(Types.VARCHAR).withSize(500));
        addMetadata(objectNumber, ColumnMetadata.named("OBJECT_NUMBER").withIndex(59).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectPathSeqno, ColumnMetadata.named("OBJECT_PATH_SEQNO").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectRow, ColumnMetadata.named("OBJECT_ROW").withIndex(29).ofType(Types.DECIMAL).withSize(22));
        addMetadata(objectSchema, ColumnMetadata.named("OBJECT_SCHEMA").withIndex(17).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectTablespace, ColumnMetadata.named("OBJECT_TABLESPACE").withIndex(27).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectType, ColumnMetadata.named("OBJECT_TYPE").withIndex(13).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objectTypePath, ColumnMetadata.named("OBJECT_TYPE_PATH").withIndex(11).ofType(Types.VARCHAR).withSize(200));
        addMetadata(oldValue, ColumnMetadata.named("OLD_VALUE").withIndex(66).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(operation, ColumnMetadata.named("OPERATION").withIndex(70).ofType(Types.VARCHAR).withSize(30));
        addMetadata(originalObjectName, ColumnMetadata.named("ORIGINAL_OBJECT_NAME").withIndex(19).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(originalObjectSchema, ColumnMetadata.named("ORIGINAL_OBJECT_SCHEMA").withIndex(18).ofType(Types.VARCHAR).withSize(30));
        addMetadata(packetNumber, ColumnMetadata.named("PACKET_NUMBER").withIndex(65).ofType(Types.DECIMAL).withSize(22));
        addMetadata(parallelization, ColumnMetadata.named("PARALLELIZATION").withIndex(38).ofType(Types.DECIMAL).withSize(22));
        addMetadata(parentProcessOrder, ColumnMetadata.named("PARENT_PROCESS_ORDER").withIndex(45).ofType(Types.DECIMAL).withSize(22));
        addMetadata(partitionName, ColumnMetadata.named("PARTITION_NAME").withIndex(20).ofType(Types.VARCHAR).withSize(30));
        addMetadata(phase, ColumnMetadata.named("PHASE").withIndex(80).ofType(Types.DECIMAL).withSize(22));
        addMetadata(platform, ColumnMetadata.named("PLATFORM").withIndex(87).ofType(Types.VARCHAR).withSize(101));
        addMetadata(processingState, ColumnMetadata.named("PROCESSING_STATE").withIndex(30).ofType(Types.CHAR).withSize(1));
        addMetadata(processingStatus, ColumnMetadata.named("PROCESSING_STATUS").withIndex(31).ofType(Types.CHAR).withSize(1));
        addMetadata(processName, ColumnMetadata.named("PROCESS_NAME").withIndex(56).ofType(Types.VARCHAR).withSize(30));
        addMetadata(processOrder, ColumnMetadata.named("PROCESS_ORDER").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(property, ColumnMetadata.named("PROPERTY").withIndex(23).ofType(Types.DECIMAL).withSize(22));
        addMetadata(remoteLink, ColumnMetadata.named("REMOTE_LINK").withIndex(74).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(scn, ColumnMetadata.named("SCN").withIndex(42).ofType(Types.DECIMAL).withSize(22));
        addMetadata(seed, ColumnMetadata.named("SEED").withIndex(67).ofType(Types.DECIMAL).withSize(22));
        addMetadata(serviceName, ColumnMetadata.named("SERVICE_NAME").withIndex(91).ofType(Types.VARCHAR).withSize(100));
        addMetadata(sizeEstimate, ColumnMetadata.named("SIZE_ESTIMATE").withIndex(28).ofType(Types.DECIMAL).withSize(22));
        addMetadata(startTime, ColumnMetadata.named("START_TIME").withIndex(82).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(79).ofType(Types.VARCHAR).withSize(30));
        addMetadata(statusQueue, ColumnMetadata.named("STATUS_QUEUE").withIndex(73).ofType(Types.VARCHAR).withSize(30));
        addMetadata(subpartitionName, ColumnMetadata.named("SUBPARTITION_NAME").withIndex(21).ofType(Types.VARCHAR).withSize(30));
        addMetadata(timezone, ColumnMetadata.named("TIMEZONE").withIndex(78).ofType(Types.VARCHAR).withSize(64));
        addMetadata(totalBytes, ColumnMetadata.named("TOTAL_BYTES").withIndex(61).ofType(Types.DECIMAL).withSize(22));
        addMetadata(trigflag, ColumnMetadata.named("TRIGFLAG").withIndex(24).ofType(Types.DECIMAL).withSize(22));
        addMetadata(unloadMethod, ColumnMetadata.named("UNLOAD_METHOD").withIndex(39).ofType(Types.DECIMAL).withSize(22));
        addMetadata(userDirectory, ColumnMetadata.named("USER_DIRECTORY").withIndex(51).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(userFileName, ColumnMetadata.named("USER_FILE_NAME").withIndex(52).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(userName, ColumnMetadata.named("USER_NAME").withIndex(69).ofType(Types.VARCHAR).withSize(30));
        addMetadata(valueN, ColumnMetadata.named("VALUE_N").withIndex(48).ofType(Types.DECIMAL).withSize(22));
        addMetadata(valueT, ColumnMetadata.named("VALUE_T").withIndex(47).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(75).ofType(Types.DECIMAL).withSize(22));
        addMetadata(workItem, ColumnMetadata.named("WORK_ITEM").withIndex(58).ofType(Types.VARCHAR).withSize(30));
        addMetadata(xmlClob, ColumnMetadata.named("XML_CLOB").withIndex(44).ofType(Types.CLOB).withSize(4000));
    }

}

