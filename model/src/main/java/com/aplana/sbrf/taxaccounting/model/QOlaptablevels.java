package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOlaptablevels is a Querydsl query type for QOlaptablevels
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOlaptablevels extends com.querydsl.sql.RelationalPathBase<QOlaptablevels> {

    private static final long serialVersionUID = 1654008315;

    public static final QOlaptablevels olaptablevels = new QOlaptablevels("OLAPTABLEVELS");

    public final StringPath cubeName = createString("cubeName");

    public final StringPath dimensionName = createString("dimensionName");

    public final StringPath dimensionOwner = createString("dimensionOwner");

    public final StringPath levelName = createString("levelName");

    public final StringPath schemaName = createString("schemaName");

    public final NumberPath<java.math.BigInteger> selected = createNumber("selected", java.math.BigInteger.class);

    public QOlaptablevels(String variable) {
        super(QOlaptablevels.class, forVariable(variable), "SYS", "OLAPTABLEVELS");
        addMetadata();
    }

    public QOlaptablevels(String variable, String schema, String table) {
        super(QOlaptablevels.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOlaptablevels(Path<? extends QOlaptablevels> path) {
        super(path.getType(), path.getMetadata(), "SYS", "OLAPTABLEVELS");
        addMetadata();
    }

    public QOlaptablevels(PathMetadata metadata) {
        super(QOlaptablevels.class, metadata, "SYS", "OLAPTABLEVELS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cubeName, ColumnMetadata.named("CUBE_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(30));
        addMetadata(dimensionName, ColumnMetadata.named("DIMENSION_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(30));
        addMetadata(dimensionOwner, ColumnMetadata.named("DIMENSION_OWNER").withIndex(3).ofType(Types.VARCHAR).withSize(30));
        addMetadata(levelName, ColumnMetadata.named("LEVEL_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(30));
        addMetadata(schemaName, ColumnMetadata.named("SCHEMA_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(selected, ColumnMetadata.named("SELECTED").withIndex(6).ofType(Types.DECIMAL).withSize(22));
    }

}

