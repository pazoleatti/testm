package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoGeorPluginRegistry is a Querydsl query type for QSdoGeorPluginRegistry
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoGeorPluginRegistry extends com.querydsl.sql.RelationalPathBase<QSdoGeorPluginRegistry> {

    private static final long serialVersionUID = -1066729966;

    public static final QSdoGeorPluginRegistry sdoGeorPluginRegistry = new QSdoGeorPluginRegistry("SDO_GEOR_PLUGIN_REGISTRY");

    public final StringPath companyName = createString("companyName");

    public final StringPath description = createString("description");

    public final StringPath plugin = createString("plugin");

    public final StringPath pluginName = createString("pluginName");

    public final StringPath pluginType = createString("pluginType");

    public final com.querydsl.sql.PrimaryKey<QSdoGeorPluginRegistry> sysC005585 = createPrimaryKey(pluginName);

    public QSdoGeorPluginRegistry(String variable) {
        super(QSdoGeorPluginRegistry.class, forVariable(variable), "MDSYS", "SDO_GEOR_PLUGIN_REGISTRY");
        addMetadata();
    }

    public QSdoGeorPluginRegistry(String variable, String schema, String table) {
        super(QSdoGeorPluginRegistry.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoGeorPluginRegistry(Path<? extends QSdoGeorPluginRegistry> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_GEOR_PLUGIN_REGISTRY");
        addMetadata();
    }

    public QSdoGeorPluginRegistry(PathMetadata metadata) {
        super(QSdoGeorPluginRegistry.class, metadata, "MDSYS", "SDO_GEOR_PLUGIN_REGISTRY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(companyName, ColumnMetadata.named("COMPANY_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(5).ofType(Types.VARCHAR).withSize(1024));
        addMetadata(plugin, ColumnMetadata.named("PLUGIN").withIndex(3).ofType(Types.VARCHAR).withSize(32));
        addMetadata(pluginName, ColumnMetadata.named("PLUGIN_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(pluginType, ColumnMetadata.named("PLUGIN_TYPE").withIndex(2).ofType(Types.VARCHAR).withSize(32));
    }

}

