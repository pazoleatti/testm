package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMview$AdvOwb is a Querydsl query type for QMview$AdvOwb
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMview$AdvOwb extends com.querydsl.sql.RelationalPathBase<QMview$AdvOwb> {

    private static final long serialVersionUID = -1649482794;

    public static final QMview$AdvOwb mview$AdvOwb = new QMview$AdvOwb("MVIEW$_ADV_OWB");

    public final StringPath indexscript = createString("indexscript");

    public final StringPath mvscript = createString("mvscript");

    public final StringPath objname = createString("objname");

    public final StringPath ownername = createString("ownername");

    public final NumberPath<java.math.BigInteger> runid_ = createNumber("runid_", java.math.BigInteger.class);

    public QMview$AdvOwb(String variable) {
        super(QMview$AdvOwb.class, forVariable(variable), "SYSTEM", "MVIEW$_ADV_OWB");
        addMetadata();
    }

    public QMview$AdvOwb(String variable, String schema, String table) {
        super(QMview$AdvOwb.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMview$AdvOwb(Path<? extends QMview$AdvOwb> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "MVIEW$_ADV_OWB");
        addMetadata();
    }

    public QMview$AdvOwb(PathMetadata metadata) {
        super(QMview$AdvOwb.class, metadata, "SYSTEM", "MVIEW$_ADV_OWB");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(indexscript, ColumnMetadata.named("INDEXSCRIPT").withIndex(5).ofType(Types.CLOB).withSize(4000));
        addMetadata(mvscript, ColumnMetadata.named("MVSCRIPT").withIndex(4).ofType(Types.CLOB).withSize(4000));
        addMetadata(objname, ColumnMetadata.named("OBJNAME").withIndex(2).ofType(Types.VARCHAR).withSize(30));
        addMetadata(ownername, ColumnMetadata.named("OWNERNAME").withIndex(3).ofType(Types.VARCHAR).withSize(30));
        addMetadata(runid_, ColumnMetadata.named("RUNID#").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

