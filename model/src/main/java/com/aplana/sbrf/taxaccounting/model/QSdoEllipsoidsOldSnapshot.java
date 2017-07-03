package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoEllipsoidsOldSnapshot is a Querydsl query type for QSdoEllipsoidsOldSnapshot
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoEllipsoidsOldSnapshot extends com.querydsl.sql.RelationalPathBase<QSdoEllipsoidsOldSnapshot> {

    private static final long serialVersionUID = 1338806682;

    public static final QSdoEllipsoidsOldSnapshot sdoEllipsoidsOldSnapshot = new QSdoEllipsoidsOldSnapshot("SDO_ELLIPSOIDS_OLD_SNAPSHOT");

    public final NumberPath<java.math.BigInteger> inverseFlattening = createNumber("inverseFlattening", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> semiMajorAxis = createNumber("semiMajorAxis", java.math.BigInteger.class);

    public QSdoEllipsoidsOldSnapshot(String variable) {
        super(QSdoEllipsoidsOldSnapshot.class, forVariable(variable), "MDSYS", "SDO_ELLIPSOIDS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoEllipsoidsOldSnapshot(String variable, String schema, String table) {
        super(QSdoEllipsoidsOldSnapshot.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoEllipsoidsOldSnapshot(Path<? extends QSdoEllipsoidsOldSnapshot> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_ELLIPSOIDS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoEllipsoidsOldSnapshot(PathMetadata metadata) {
        super(QSdoEllipsoidsOldSnapshot.class, metadata, "MDSYS", "SDO_ELLIPSOIDS_OLD_SNAPSHOT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(inverseFlattening, ColumnMetadata.named("INVERSE_FLATTENING").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(64));
        addMetadata(semiMajorAxis, ColumnMetadata.named("SEMI_MAJOR_AXIS").withIndex(2).ofType(Types.DECIMAL).withSize(22));
    }

}

