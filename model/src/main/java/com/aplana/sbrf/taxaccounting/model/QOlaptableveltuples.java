package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOlaptableveltuples is a Querydsl query type for QOlaptableveltuples
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOlaptableveltuples extends com.querydsl.sql.RelationalPathBase<QOlaptableveltuples> {

    private static final long serialVersionUID = 1988492579;

    public static final QOlaptableveltuples olaptableveltuples = new QOlaptableveltuples("OLAPTABLEVELTUPLES");

    public final StringPath cubeName = createString("cubeName");

    public final StringPath dimensionName = createString("dimensionName");

    public final StringPath dimensionOwner = createString("dimensionOwner");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final StringPath levelName = createString("levelName");

    public final NumberPath<java.math.BigInteger> pctOfTotal = createNumber("pctOfTotal", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rowCount = createNumber("rowCount", java.math.BigInteger.class);

    public final StringPath schemaName = createString("schemaName");

    public final NumberPath<java.math.BigInteger> selected = createNumber("selected", java.math.BigInteger.class);

    public QOlaptableveltuples(String variable) {
        super(QOlaptableveltuples.class, forVariable(variable), "SYS", "OLAPTABLEVELTUPLES");
        addMetadata();
    }

    public QOlaptableveltuples(String variable, String schema, String table) {
        super(QOlaptableveltuples.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOlaptableveltuples(Path<? extends QOlaptableveltuples> path) {
        super(path.getType(), path.getMetadata(), "SYS", "OLAPTABLEVELTUPLES");
        addMetadata();
    }

    public QOlaptableveltuples(PathMetadata metadata) {
        super(QOlaptableveltuples.class, metadata, "SYS", "OLAPTABLEVELTUPLES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cubeName, ColumnMetadata.named("CUBE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(30));
        addMetadata(dimensionName, ColumnMetadata.named("DIMENSION_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(30));
        addMetadata(dimensionOwner, ColumnMetadata.named("DIMENSION_OWNER").withIndex(7).ofType(Types.VARCHAR).withSize(30));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(levelName, ColumnMetadata.named("LEVEL_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(30));
        addMetadata(pctOfTotal, ColumnMetadata.named("PCT_OF_TOTAL").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rowCount, ColumnMetadata.named("ROW_COUNT").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(schemaName, ColumnMetadata.named("SCHEMA_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(30));
        addMetadata(selected, ColumnMetadata.named("SELECTED").withIndex(9).ofType(Types.DECIMAL).withSize(22));
    }

}

