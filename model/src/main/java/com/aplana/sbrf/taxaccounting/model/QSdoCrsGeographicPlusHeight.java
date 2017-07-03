package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCrsGeographicPlusHeight is a Querydsl query type for QSdoCrsGeographicPlusHeight
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCrsGeographicPlusHeight extends com.querydsl.sql.RelationalPathBase<QSdoCrsGeographicPlusHeight> {

    private static final long serialVersionUID = -1886126597;

    public static final QSdoCrsGeographicPlusHeight sdoCrsGeographicPlusHeight = new QSdoCrsGeographicPlusHeight("SDO_CRS_GEOGRAPHIC_PLUS_HEIGHT");

    public final NumberPath<Long> srid = createNumber("srid", Long.class);

    public QSdoCrsGeographicPlusHeight(String variable) {
        super(QSdoCrsGeographicPlusHeight.class, forVariable(variable), "MDSYS", "SDO_CRS_GEOGRAPHIC_PLUS_HEIGHT");
        addMetadata();
    }

    public QSdoCrsGeographicPlusHeight(String variable, String schema, String table) {
        super(QSdoCrsGeographicPlusHeight.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCrsGeographicPlusHeight(Path<? extends QSdoCrsGeographicPlusHeight> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_CRS_GEOGRAPHIC_PLUS_HEIGHT");
        addMetadata();
    }

    public QSdoCrsGeographicPlusHeight(PathMetadata metadata) {
        super(QSdoCrsGeographicPlusHeight.class, metadata, "MDSYS", "SDO_CRS_GEOGRAPHIC_PLUS_HEIGHT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(1).ofType(Types.DECIMAL).withSize(10));
    }

}

