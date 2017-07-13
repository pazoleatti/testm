package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QConfigurationEmail is a Querydsl query type for QConfigurationEmail
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QConfigurationEmail extends com.querydsl.sql.RelationalPathBase<QConfigurationEmail> {

    private static final long serialVersionUID = 1295962147;

    public static final QConfigurationEmail configurationEmail = new QConfigurationEmail("CONFIGURATION_EMAIL");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<QConfigurationEmail> configurationEmailPk = createPrimaryKey(id);

    public QConfigurationEmail(String variable) {
        super(QConfigurationEmail.class, forVariable(variable), "NDFL_UNSTABLE", "CONFIGURATION_EMAIL");
        addMetadata();
    }

    public QConfigurationEmail(String variable, String schema, String table) {
        super(QConfigurationEmail.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QConfigurationEmail(Path<? extends QConfigurationEmail> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "CONFIGURATION_EMAIL");
        addMetadata();
    }

    public QConfigurationEmail(PathMetadata metadata) {
        super(QConfigurationEmail.class, metadata, "NDFL_UNSTABLE", "CONFIGURATION_EMAIL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(200));
    }

}

