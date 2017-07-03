package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoDatumsOldSnapshot is a Querydsl query type for QSdoDatumsOldSnapshot
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoDatumsOldSnapshot extends com.querydsl.sql.RelationalPathBase<QSdoDatumsOldSnapshot> {

    private static final long serialVersionUID = 2037915782;

    public static final QSdoDatumsOldSnapshot sdoDatumsOldSnapshot = new QSdoDatumsOldSnapshot("SDO_DATUMS_OLD_SNAPSHOT");

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> rotateX = createNumber("rotateX", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rotateY = createNumber("rotateY", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rotateZ = createNumber("rotateZ", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> scaleAdjust = createNumber("scaleAdjust", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftX = createNumber("shiftX", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftY = createNumber("shiftY", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> shiftZ = createNumber("shiftZ", java.math.BigInteger.class);

    public QSdoDatumsOldSnapshot(String variable) {
        super(QSdoDatumsOldSnapshot.class, forVariable(variable), "MDSYS", "SDO_DATUMS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoDatumsOldSnapshot(String variable, String schema, String table) {
        super(QSdoDatumsOldSnapshot.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoDatumsOldSnapshot(Path<? extends QSdoDatumsOldSnapshot> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_DATUMS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoDatumsOldSnapshot(PathMetadata metadata) {
        super(QSdoDatumsOldSnapshot.class, metadata, "MDSYS", "SDO_DATUMS_OLD_SNAPSHOT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(64));
        addMetadata(rotateX, ColumnMetadata.named("ROTATE_X").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rotateY, ColumnMetadata.named("ROTATE_Y").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(rotateZ, ColumnMetadata.named("ROTATE_Z").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(scaleAdjust, ColumnMetadata.named("SCALE_ADJUST").withIndex(8).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftX, ColumnMetadata.named("SHIFT_X").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftY, ColumnMetadata.named("SHIFT_Y").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(shiftZ, ColumnMetadata.named("SHIFT_Z").withIndex(4).ofType(Types.DECIMAL).withSize(22));
    }

}

