package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOgisGeometryColumns is a Querydsl query type for QOgisGeometryColumns
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOgisGeometryColumns extends com.querydsl.sql.RelationalPathBase<QOgisGeometryColumns> {

    private static final long serialVersionUID = 1215737196;

    public static final QOgisGeometryColumns ogisGeometryColumns = new QOgisGeometryColumns("OGIS_GEOMETRY_COLUMNS");

    public final NumberPath<java.math.BigInteger> coordDimension = createNumber("coordDimension", java.math.BigInteger.class);

    public final StringPath fGeometryColumn = createString("fGeometryColumn");

    public final StringPath fTableName = createString("fTableName");

    public final StringPath fTableSchema = createString("fTableSchema");

    public final NumberPath<java.math.BigInteger> geometryType = createNumber("geometryType", java.math.BigInteger.class);

    public final StringPath gTableName = createString("gTableName");

    public final StringPath gTableSchema = createString("gTableSchema");

    public final NumberPath<java.math.BigInteger> maxPpr = createNumber("maxPpr", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> srid = createNumber("srid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> storageType = createNumber("storageType", java.math.BigInteger.class);

    public final com.querydsl.sql.ForeignKey<QOgisSpatialReferenceSystems> sridFk = createForeignKey(srid, "SRID");

    public QOgisGeometryColumns(String variable) {
        super(QOgisGeometryColumns.class, forVariable(variable), "MDSYS", "OGIS_GEOMETRY_COLUMNS");
        addMetadata();
    }

    public QOgisGeometryColumns(String variable, String schema, String table) {
        super(QOgisGeometryColumns.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOgisGeometryColumns(Path<? extends QOgisGeometryColumns> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "OGIS_GEOMETRY_COLUMNS");
        addMetadata();
    }

    public QOgisGeometryColumns(PathMetadata metadata) {
        super(QOgisGeometryColumns.class, metadata, "MDSYS", "OGIS_GEOMETRY_COLUMNS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordDimension, ColumnMetadata.named("COORD_DIMENSION").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(fGeometryColumn, ColumnMetadata.named("F_GEOMETRY_COLUMN").withIndex(3).ofType(Types.VARCHAR).withSize(64));
        addMetadata(fTableName, ColumnMetadata.named("F_TABLE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(64));
        addMetadata(fTableSchema, ColumnMetadata.named("F_TABLE_SCHEMA").withIndex(1).ofType(Types.VARCHAR).withSize(64));
        addMetadata(geometryType, ColumnMetadata.named("GEOMETRY_TYPE").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(gTableName, ColumnMetadata.named("G_TABLE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(64));
        addMetadata(gTableSchema, ColumnMetadata.named("G_TABLE_SCHEMA").withIndex(4).ofType(Types.VARCHAR).withSize(64));
        addMetadata(maxPpr, ColumnMetadata.named("MAX_PPR").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(storageType, ColumnMetadata.named("STORAGE_TYPE").withIndex(6).ofType(Types.DECIMAL).withSize(22));
    }

}

