package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoXmlSchemas is a Querydsl query type for QSdoXmlSchemas
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoXmlSchemas extends com.querydsl.sql.RelationalPathBase<QSdoXmlSchemas> {

    private static final long serialVersionUID = 1556855740;

    public static final QSdoXmlSchemas sdoXmlSchemas = new QSdoXmlSchemas("SDO_XML_SCHEMAS");

    public final StringPath description = createString("description");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath xmlschema = createString("xmlschema");

    public final com.querydsl.sql.PrimaryKey<QSdoXmlSchemas> sysC004892 = createPrimaryKey(id);

    public QSdoXmlSchemas(String variable) {
        super(QSdoXmlSchemas.class, forVariable(variable), "MDSYS", "SDO_XML_SCHEMAS");
        addMetadata();
    }

    public QSdoXmlSchemas(String variable, String schema, String table) {
        super(QSdoXmlSchemas.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoXmlSchemas(Path<? extends QSdoXmlSchemas> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_XML_SCHEMAS");
        addMetadata();
    }

    public QSdoXmlSchemas(PathMetadata metadata) {
        super(QSdoXmlSchemas.class, metadata, "MDSYS", "SDO_XML_SCHEMAS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(2).ofType(Types.VARCHAR).withSize(300));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(xmlschema, ColumnMetadata.named("XMLSCHEMA").withIndex(3).ofType(Types.CLOB).withSize(4000));
    }

}

