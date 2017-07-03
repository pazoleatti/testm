package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNtv2XmlData is a Querydsl query type for QNtv2XmlData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNtv2XmlData extends com.querydsl.sql.RelationalPathBase<QNtv2XmlData> {

    private static final long serialVersionUID = -119794174;

    public static final QNtv2XmlData ntv2XmlData = new QNtv2XmlData("NTV2_XML_DATA");

    public final NumberPath<java.math.BigInteger> ntv2FileId = createNumber("ntv2FileId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> sequenceNumber = createNumber("sequenceNumber", java.math.BigInteger.class);

    public final SimplePath<Object> xml = createSimple("xml", Object.class);

    public final com.querydsl.sql.PrimaryKey<QNtv2XmlData> ntv2XmlDataPk = createPrimaryKey(ntv2FileId, sequenceNumber);

    public QNtv2XmlData(String variable) {
        super(QNtv2XmlData.class, forVariable(variable), "MDSYS", "NTV2_XML_DATA");
        addMetadata();
    }

    public QNtv2XmlData(String variable, String schema, String table) {
        super(QNtv2XmlData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNtv2XmlData(Path<? extends QNtv2XmlData> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "NTV2_XML_DATA");
        addMetadata();
    }

    public QNtv2XmlData(PathMetadata metadata) {
        super(QNtv2XmlData.class, metadata, "MDSYS", "NTV2_XML_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(ntv2FileId, ColumnMetadata.named("NTV2_FILE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(sequenceNumber, ColumnMetadata.named("SEQUENCE_NUMBER").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(xml, ColumnMetadata.named("XML").withIndex(3).ofType(2007).withSize(2000).notNull());
    }

}

