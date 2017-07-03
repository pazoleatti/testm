package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGeorXmlschemaTable is a Querydsl query type for QSdoGeorXmlschemaTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGeorXmlschemaTable extends com.querydsl.sql.RelationalPathBase<QSdoGeorXmlschemaTable> {

    private static final long serialVersionUID = -1854055496;

    public static final QSdoGeorXmlschemaTable sdoGeorXmlschemaTable = new QSdoGeorXmlschemaTable("SDO_GEOR_XMLSCHEMA_TABLE");

    public final StringPath georasterformat = createString("georasterformat");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath xmlschema = createString("xmlschema");

    public final com.querydsl.sql.PrimaryKey<QSdoGeorXmlschemaTable> sysC005577 = createPrimaryKey(id);

    public QSdoGeorXmlschemaTable(String variable) {
        super(QSdoGeorXmlschemaTable.class, forVariable(variable), "MDSYS", "SDO_GEOR_XMLSCHEMA_TABLE");
        addMetadata();
    }

    public QSdoGeorXmlschemaTable(String variable, String schema, String table) {
        super(QSdoGeorXmlschemaTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGeorXmlschemaTable(Path<? extends QSdoGeorXmlschemaTable> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GEOR_XMLSCHEMA_TABLE");
        addMetadata();
    }

    public QSdoGeorXmlschemaTable(PathMetadata metadata) {
        super(QSdoGeorXmlschemaTable.class, metadata, "MDSYS", "SDO_GEOR_XMLSCHEMA_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(georasterformat, ColumnMetadata.named("GEORASTERFORMAT").withIndex(2).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(xmlschema, ColumnMetadata.named("XMLSCHEMA").withIndex(3).ofType(Types.CLOB).withSize(4000));
    }

}

