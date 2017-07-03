package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCsSrs is a Querydsl query type for QSdoCsSrs
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCsSrs extends com.querydsl.sql.RelationalPathBase<QSdoCsSrs> {

    private static final long serialVersionUID = 379579875;

    public static final QSdoCsSrs sdoCsSrs = new QSdoCsSrs("SDO_CS_SRS");

    public final StringPath authName = createString("authName");

    public final NumberPath<java.math.BigInteger> authSrid = createNumber("authSrid", java.math.BigInteger.class);

    public final SimplePath<Object> csBounds = createSimple("csBounds", Object.class);

    public final StringPath csName = createString("csName");

    public final NumberPath<java.math.BigInteger> srid = createNumber("srid", java.math.BigInteger.class);

    public final StringPath wktext = createString("wktext");

    public final StringPath wktext3d = createString("wktext3d");

    public final com.querydsl.sql.PrimaryKey<QSdoCsSrs> sysC005529 = createPrimaryKey(srid);

    public QSdoCsSrs(String variable) {
        super(QSdoCsSrs.class, forVariable(variable), "MDSYS", "SDO_CS_SRS");
        addMetadata();
    }

    public QSdoCsSrs(String variable, String schema, String table) {
        super(QSdoCsSrs.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCsSrs(Path<? extends QSdoCsSrs> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_CS_SRS");
        addMetadata();
    }

    public QSdoCsSrs(PathMetadata metadata) {
        super(QSdoCsSrs.class, metadata, "MDSYS", "SDO_CS_SRS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authName, ColumnMetadata.named("AUTH_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(authSrid, ColumnMetadata.named("AUTH_SRID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(csBounds, ColumnMetadata.named("CS_BOUNDS").withIndex(6).ofType(Types.OTHER).withSize(1));
        addMetadata(csName, ColumnMetadata.named("CS_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(80));
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(wktext, ColumnMetadata.named("WKTEXT").withIndex(5).ofType(Types.VARCHAR).withSize(2046));
        addMetadata(wktext3d, ColumnMetadata.named("WKTEXT3D").withIndex(7).ofType(Types.VARCHAR).withSize(4000));
    }

}

