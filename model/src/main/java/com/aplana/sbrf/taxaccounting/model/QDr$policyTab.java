package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDr$policyTab is a Querydsl query type for QDr$policyTab
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDr$policyTab extends com.querydsl.sql.RelationalPathBase<QDr$policyTab> {

    private static final long serialVersionUID = 1911489034;

    public static final QDr$policyTab dr$policyTab = new QDr$policyTab("DR$POLICY_TAB");

    public final StringPath pltLangcol = createString("pltLangcol");

    public final StringPath pltPolicy = createString("pltPolicy");

    public QDr$policyTab(String variable) {
        super(QDr$policyTab.class, forVariable(variable), "CTXSYS", "DR$POLICY_TAB");
        addMetadata();
    }

    public QDr$policyTab(String variable, String schema, String table) {
        super(QDr$policyTab.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDr$policyTab(Path<? extends QDr$policyTab> path) {
        super(path.getType(), path.getMetadata(), "CTXSYS", "DR$POLICY_TAB");
        addMetadata();
    }

    public QDr$policyTab(PathMetadata metadata) {
        super(QDr$policyTab.class, metadata, "CTXSYS", "DR$POLICY_TAB");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(pltLangcol, ColumnMetadata.named("PLT_LANGCOL").withIndex(2).ofType(Types.CHAR).withSize(1));
        addMetadata(pltPolicy, ColumnMetadata.named("PLT_POLICY").withIndex(1).ofType(Types.CHAR).withSize(1));
    }

}

