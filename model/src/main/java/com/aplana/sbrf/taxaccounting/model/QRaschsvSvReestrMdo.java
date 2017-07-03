package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvReestrMdo is a Querydsl query type for QRaschsvSvReestrMdo
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvReestrMdo extends com.querydsl.sql.RelationalPathBase<QRaschsvSvReestrMdo> {

    private static final long serialVersionUID = -1029991865;

    public static final QRaschsvSvReestrMdo raschsvSvReestrMdo = new QRaschsvSvReestrMdo("RASCHSV_SV_REESTR_MDO");

    public final DateTimePath<org.joda.time.DateTime> dataZapis = createDateTime("dataZapis", org.joda.time.DateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath naimMdo = createString("naimMdo");

    public final StringPath nomerZapis = createString("nomerZapis");

    public final NumberPath<Long> raschsvSvedObuchId = createNumber("raschsvSvedObuchId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvReestrMdo> raschsvSvReestrMdoPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvedObuch> raschsvSvReestrMdoObFk = createForeignKey(raschsvSvedObuchId, "ID");

    public QRaschsvSvReestrMdo(String variable) {
        super(QRaschsvSvReestrMdo.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_REESTR_MDO");
        addMetadata();
    }

    public QRaschsvSvReestrMdo(String variable, String schema, String table) {
        super(QRaschsvSvReestrMdo.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvReestrMdo(Path<? extends QRaschsvSvReestrMdo> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_REESTR_MDO");
        addMetadata();
    }

    public QRaschsvSvReestrMdo(PathMetadata metadata) {
        super(QRaschsvSvReestrMdo.class, metadata, "NDFL_1_0", "RASCHSV_SV_REESTR_MDO");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataZapis, ColumnMetadata.named("DATA_ZAPIS").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(naimMdo, ColumnMetadata.named("NAIM_MDO").withIndex(3).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(nomerZapis, ColumnMetadata.named("NOMER_ZAPIS").withIndex(4).ofType(Types.VARCHAR).withSize(28));
        addMetadata(raschsvSvedObuchId, ColumnMetadata.named("RASCHSV_SVED_OBUCH_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

