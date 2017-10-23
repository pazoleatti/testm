package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVwDepartKppOktmo is a Querydsl query type for QVwDepartKppOktmo
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QVwDepartKppOktmo extends com.querydsl.sql.RelationalPathBase<QVwDepartKppOktmo> {

    private static final long serialVersionUID = 655638416;

    public static final QVwDepartKppOktmo vwDepartKppOktmo = new QVwDepartKppOktmo("VW_DEPART_KPP_OKTMO");

    public final NumberPath<Integer> depId = createNumber("depId", Integer.class);

    public final StringPath depName = createString("depName");

    public final StringPath kpp = createString("kpp");

    public final StringPath oktmo = createString("oktmo");

    public QVwDepartKppOktmo(String variable) {
        super(QVwDepartKppOktmo.class, forVariable(variable), "NDFL_UNSTABLE", "VW_DEPART_KPP_OKTMO");
        addMetadata();
    }

    public QVwDepartKppOktmo(String variable, String schema, String table) {
        super(QVwDepartKppOktmo.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVwDepartKppOktmo(String variable, String schema) {
        super(QVwDepartKppOktmo.class, forVariable(variable), schema, "VW_DEPART_KPP_OKTMO");
        addMetadata();
    }

    public QVwDepartKppOktmo(Path<? extends QVwDepartKppOktmo> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "VW_DEPART_KPP_OKTMO");
        addMetadata();
    }

    public QVwDepartKppOktmo(PathMetadata metadata) {
        super(QVwDepartKppOktmo.class, metadata, "NDFL_UNSTABLE", "VW_DEPART_KPP_OKTMO");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(depId, ColumnMetadata.named("DEP_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(depName, ColumnMetadata.named("DEP_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(510).notNull());
        addMetadata(kpp, ColumnMetadata.named("KPP").withIndex(3).ofType(Types.VARCHAR).withSize(9));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(4).ofType(Types.VARCHAR).withSize(11).notNull());
    }

}

