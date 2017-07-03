package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOgisSpatialReferenceSystems is a Querydsl query type for QOgisSpatialReferenceSystems
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOgisSpatialReferenceSystems extends com.querydsl.sql.RelationalPathBase<QOgisSpatialReferenceSystems> {

    private static final long serialVersionUID = -237083426;

    public static final QOgisSpatialReferenceSystems ogisSpatialReferenceSystems = new QOgisSpatialReferenceSystems("OGIS_SPATIAL_REFERENCE_SYSTEMS");

    public final StringPath authName = createString("authName");

    public final NumberPath<java.math.BigInteger> authSrid = createNumber("authSrid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> srid = createNumber("srid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> srnum = createNumber("srnum", java.math.BigInteger.class);

    public final StringPath srtext = createString("srtext");

    public final com.querydsl.sql.PrimaryKey<QOgisSpatialReferenceSystems> sridPk = createPrimaryKey(srid);

    public final com.querydsl.sql.ForeignKey<QOgisGeometryColumns> _sridFk = createInvForeignKey(srid, "SRID");

    public QOgisSpatialReferenceSystems(String variable) {
        super(QOgisSpatialReferenceSystems.class, forVariable(variable), "MDSYS", "OGIS_SPATIAL_REFERENCE_SYSTEMS");
        addMetadata();
    }

    public QOgisSpatialReferenceSystems(String variable, String schema, String table) {
        super(QOgisSpatialReferenceSystems.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOgisSpatialReferenceSystems(Path<? extends QOgisSpatialReferenceSystems> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "OGIS_SPATIAL_REFERENCE_SYSTEMS");
        addMetadata();
    }

    public QOgisSpatialReferenceSystems(PathMetadata metadata) {
        super(QOgisSpatialReferenceSystems.class, metadata, "MDSYS", "OGIS_SPATIAL_REFERENCE_SYSTEMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authName, ColumnMetadata.named("AUTH_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(100));
        addMetadata(authSrid, ColumnMetadata.named("AUTH_SRID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(srnum, ColumnMetadata.named("SRNUM").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(srtext, ColumnMetadata.named("SRTEXT").withIndex(4).ofType(Types.VARCHAR).withSize(1000));
    }

}

