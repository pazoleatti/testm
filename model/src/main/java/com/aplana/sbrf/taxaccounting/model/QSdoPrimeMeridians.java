package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoPrimeMeridians is a Querydsl query type for QSdoPrimeMeridians
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoPrimeMeridians extends com.querydsl.sql.RelationalPathBase<QSdoPrimeMeridians> {

    private static final long serialVersionUID = 1366587252;

    public static final QSdoPrimeMeridians sdoPrimeMeridians = new QSdoPrimeMeridians("SDO_PRIME_MERIDIANS");

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<Float> greenwichLongitude = createNumber("greenwichLongitude", Float.class);

    public final StringPath informationSource = createString("informationSource");

    public final NumberPath<Long> primeMeridianId = createNumber("primeMeridianId", Long.class);

    public final StringPath primeMeridianName = createString("primeMeridianName");

    public final NumberPath<Long> uomId = createNumber("uomId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSdoPrimeMeridians> primeMeridianPrim = createPrimaryKey(primeMeridianId);

    public final com.querydsl.sql.ForeignKey<QSdoUnitsOfMeasure> primeMeridianForeignUom = createForeignKey(uomId, "UOM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoDatums> _datumForeignMeridian = createInvForeignKey(primeMeridianId, "PRIME_MERIDIAN_ID");

    public QSdoPrimeMeridians(String variable) {
        super(QSdoPrimeMeridians.class, forVariable(variable), "MDSYS", "SDO_PRIME_MERIDIANS");
        addMetadata();
    }

    public QSdoPrimeMeridians(String variable, String schema, String table) {
        super(QSdoPrimeMeridians.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoPrimeMeridians(Path<? extends QSdoPrimeMeridians> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_PRIME_MERIDIANS");
        addMetadata();
    }

    public QSdoPrimeMeridians(PathMetadata metadata) {
        super(QSdoPrimeMeridians.class, metadata, "MDSYS", "SDO_PRIME_MERIDIANS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(6).ofType(Types.VARCHAR).withSize(254));
        addMetadata(greenwichLongitude, ColumnMetadata.named("GREENWICH_LONGITUDE").withIndex(3).ofType(Types.FLOAT).withSize(49));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(5).ofType(Types.VARCHAR).withSize(254));
        addMetadata(primeMeridianId, ColumnMetadata.named("PRIME_MERIDIAN_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(primeMeridianName, ColumnMetadata.named("PRIME_MERIDIAN_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(uomId, ColumnMetadata.named("UOM_ID").withIndex(4).ofType(Types.DECIMAL).withSize(10));
    }

}

