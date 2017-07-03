package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSrsnamespaceTable is a Querydsl query type for QSrsnamespaceTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSrsnamespaceTable extends com.querydsl.sql.RelationalPathBase<QSrsnamespaceTable> {

    private static final long serialVersionUID = 141306538;

    public static final QSrsnamespaceTable srsnamespaceTable = new QSrsnamespaceTable("SRSNAMESPACE_TABLE");

    public final NumberPath<java.math.BigInteger> sdoSrid = createNumber("sdoSrid", java.math.BigInteger.class);

    public final StringPath srsname = createString("srsname");

    public final StringPath srsnamespace = createString("srsnamespace");

    public final com.querydsl.sql.PrimaryKey<QSrsnamespaceTable> sysC004731 = createPrimaryKey(srsname, srsnamespace);

    public QSrsnamespaceTable(String variable) {
        super(QSrsnamespaceTable.class, forVariable(variable), "MDSYS", "SRSNAMESPACE_TABLE");
        addMetadata();
    }

    public QSrsnamespaceTable(String variable, String schema, String table) {
        super(QSrsnamespaceTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSrsnamespaceTable(Path<? extends QSrsnamespaceTable> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SRSNAMESPACE_TABLE");
        addMetadata();
    }

    public QSrsnamespaceTable(PathMetadata metadata) {
        super(QSrsnamespaceTable.class, metadata, "MDSYS", "SRSNAMESPACE_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sdoSrid, ColumnMetadata.named("SDO_SRID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(srsname, ColumnMetadata.named("SRSNAME").withIndex(2).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(srsnamespace, ColumnMetadata.named("SRSNAMESPACE").withIndex(1).ofType(Types.VARCHAR).withSize(2000).notNull());
    }

}

