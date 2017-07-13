package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvItogStrahLic is a Querydsl query type for QRaschsvItogStrahLic
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvItogStrahLic extends com.querydsl.sql.RelationalPathBase<QRaschsvItogStrahLic> {

    private static final long serialVersionUID = -776255424;

    public static final QRaschsvItogStrahLic raschsvItogStrahLic = new QRaschsvItogStrahLic("RASCHSV_ITOG_STRAH_LIC");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigInteger> kolLic = createNumber("kolLic", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvItogStrahLic> raschsvItogStrahLicPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvItogVyplDop> _raschsvItogVyplDopStrahFk = createInvForeignKey(id, "RASCHSV_ITOG_STRAH_LIC_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvItogVypl> _raschsvItogVyplStrahFk = createInvForeignKey(id, "RASCHSV_ITOG_STRAH_LIC_ID");

    public QRaschsvItogStrahLic(String variable) {
        super(QRaschsvItogStrahLic.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_ITOG_STRAH_LIC");
        addMetadata();
    }

    public QRaschsvItogStrahLic(String variable, String schema, String table) {
        super(QRaschsvItogStrahLic.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvItogStrahLic(Path<? extends QRaschsvItogStrahLic> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_ITOG_STRAH_LIC");
        addMetadata();
    }

    public QRaschsvItogStrahLic(PathMetadata metadata) {
        super(QRaschsvItogStrahLic.class, metadata, "NDFL_UNSTABLE", "RASCHSV_ITOG_STRAH_LIC");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kolLic, ColumnMetadata.named("KOL_LIC").withIndex(3).ofType(Types.DECIMAL).withSize(20));
    }

}

