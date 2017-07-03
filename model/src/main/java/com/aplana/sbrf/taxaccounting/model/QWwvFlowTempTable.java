package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWwvFlowTempTable is a Querydsl query type for QWwvFlowTempTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWwvFlowTempTable extends com.querydsl.sql.RelationalPathBase<QWwvFlowTempTable> {

    private static final long serialVersionUID = 793974771;

    public static final QWwvFlowTempTable wwvFlowTempTable = new QWwvFlowTempTable("WWV_FLOW_TEMP_TABLE");

    public final StringPath c001 = createString("c001");

    public final StringPath c002 = createString("c002");

    public final StringPath c003 = createString("c003");

    public final StringPath c004 = createString("c004");

    public final StringPath c005 = createString("c005");

    public final StringPath c006 = createString("c006");

    public final StringPath c007 = createString("c007");

    public final StringPath c008 = createString("c008");

    public final StringPath c009 = createString("c009");

    public final StringPath c010 = createString("c010");

    public final StringPath c011 = createString("c011");

    public final StringPath c012 = createString("c012");

    public final StringPath c013 = createString("c013");

    public final StringPath c014 = createString("c014");

    public final StringPath c015 = createString("c015");

    public final StringPath c016 = createString("c016");

    public final StringPath c017 = createString("c017");

    public final StringPath c018 = createString("c018");

    public final StringPath c019 = createString("c019");

    public final StringPath c020 = createString("c020");

    public final StringPath c021 = createString("c021");

    public final StringPath c022 = createString("c022");

    public final StringPath c023 = createString("c023");

    public final StringPath c024 = createString("c024");

    public final StringPath c025 = createString("c025");

    public final StringPath c026 = createString("c026");

    public final StringPath c027 = createString("c027");

    public final StringPath c028 = createString("c028");

    public final StringPath c029 = createString("c029");

    public final StringPath c030 = createString("c030");

    public final StringPath c031 = createString("c031");

    public final StringPath c032 = createString("c032");

    public final StringPath c033 = createString("c033");

    public final StringPath c034 = createString("c034");

    public final StringPath c035 = createString("c035");

    public final StringPath c036 = createString("c036");

    public final StringPath c037 = createString("c037");

    public final StringPath c038 = createString("c038");

    public final StringPath c039 = createString("c039");

    public final StringPath c040 = createString("c040");

    public final StringPath c041 = createString("c041");

    public final StringPath c042 = createString("c042");

    public final StringPath c043 = createString("c043");

    public final StringPath c044 = createString("c044");

    public final StringPath c045 = createString("c045");

    public final StringPath c046 = createString("c046");

    public final StringPath c047 = createString("c047");

    public final StringPath c048 = createString("c048");

    public final StringPath c049 = createString("c049");

    public final StringPath c050 = createString("c050");

    public final StringPath c051 = createString("c051");

    public final StringPath c052 = createString("c052");

    public final StringPath c053 = createString("c053");

    public final StringPath c054 = createString("c054");

    public final StringPath c055 = createString("c055");

    public final StringPath c056 = createString("c056");

    public final StringPath c057 = createString("c057");

    public final StringPath c058 = createString("c058");

    public final StringPath c059 = createString("c059");

    public final StringPath c060 = createString("c060");

    public final StringPath c061 = createString("c061");

    public final StringPath c062 = createString("c062");

    public final StringPath c063 = createString("c063");

    public final StringPath c064 = createString("c064");

    public final StringPath c065 = createString("c065");

    public final NumberPath<java.math.BigInteger> r = createNumber("r", java.math.BigInteger.class);

    public QWwvFlowTempTable(String variable) {
        super(QWwvFlowTempTable.class, forVariable(variable), "APEX_030200", "WWV_FLOW_TEMP_TABLE");
        addMetadata();
    }

    public QWwvFlowTempTable(String variable, String schema, String table) {
        super(QWwvFlowTempTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWwvFlowTempTable(Path<? extends QWwvFlowTempTable> path) {
        super(path.getType(), path.getMetadata(), "APEX_030200", "WWV_FLOW_TEMP_TABLE");
        addMetadata();
    }

    public QWwvFlowTempTable(PathMetadata metadata) {
        super(QWwvFlowTempTable.class, metadata, "APEX_030200", "WWV_FLOW_TEMP_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(c001, ColumnMetadata.named("C001").withIndex(2).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c002, ColumnMetadata.named("C002").withIndex(3).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c003, ColumnMetadata.named("C003").withIndex(4).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c004, ColumnMetadata.named("C004").withIndex(5).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c005, ColumnMetadata.named("C005").withIndex(6).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c006, ColumnMetadata.named("C006").withIndex(7).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c007, ColumnMetadata.named("C007").withIndex(8).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c008, ColumnMetadata.named("C008").withIndex(9).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c009, ColumnMetadata.named("C009").withIndex(10).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c010, ColumnMetadata.named("C010").withIndex(11).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c011, ColumnMetadata.named("C011").withIndex(12).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c012, ColumnMetadata.named("C012").withIndex(13).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c013, ColumnMetadata.named("C013").withIndex(14).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c014, ColumnMetadata.named("C014").withIndex(15).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c015, ColumnMetadata.named("C015").withIndex(16).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c016, ColumnMetadata.named("C016").withIndex(17).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c017, ColumnMetadata.named("C017").withIndex(18).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c018, ColumnMetadata.named("C018").withIndex(19).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c019, ColumnMetadata.named("C019").withIndex(20).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c020, ColumnMetadata.named("C020").withIndex(21).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c021, ColumnMetadata.named("C021").withIndex(22).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c022, ColumnMetadata.named("C022").withIndex(23).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c023, ColumnMetadata.named("C023").withIndex(24).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c024, ColumnMetadata.named("C024").withIndex(25).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c025, ColumnMetadata.named("C025").withIndex(26).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c026, ColumnMetadata.named("C026").withIndex(27).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c027, ColumnMetadata.named("C027").withIndex(28).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c028, ColumnMetadata.named("C028").withIndex(29).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c029, ColumnMetadata.named("C029").withIndex(30).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c030, ColumnMetadata.named("C030").withIndex(31).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c031, ColumnMetadata.named("C031").withIndex(32).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c032, ColumnMetadata.named("C032").withIndex(33).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c033, ColumnMetadata.named("C033").withIndex(34).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c034, ColumnMetadata.named("C034").withIndex(35).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c035, ColumnMetadata.named("C035").withIndex(36).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c036, ColumnMetadata.named("C036").withIndex(37).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c037, ColumnMetadata.named("C037").withIndex(38).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c038, ColumnMetadata.named("C038").withIndex(39).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c039, ColumnMetadata.named("C039").withIndex(40).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c040, ColumnMetadata.named("C040").withIndex(41).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c041, ColumnMetadata.named("C041").withIndex(42).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c042, ColumnMetadata.named("C042").withIndex(43).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c043, ColumnMetadata.named("C043").withIndex(44).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c044, ColumnMetadata.named("C044").withIndex(45).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c045, ColumnMetadata.named("C045").withIndex(46).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c046, ColumnMetadata.named("C046").withIndex(47).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c047, ColumnMetadata.named("C047").withIndex(48).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c048, ColumnMetadata.named("C048").withIndex(49).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c049, ColumnMetadata.named("C049").withIndex(50).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c050, ColumnMetadata.named("C050").withIndex(51).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c051, ColumnMetadata.named("C051").withIndex(52).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c052, ColumnMetadata.named("C052").withIndex(53).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c053, ColumnMetadata.named("C053").withIndex(54).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c054, ColumnMetadata.named("C054").withIndex(55).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c055, ColumnMetadata.named("C055").withIndex(56).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c056, ColumnMetadata.named("C056").withIndex(57).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c057, ColumnMetadata.named("C057").withIndex(58).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c058, ColumnMetadata.named("C058").withIndex(59).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c059, ColumnMetadata.named("C059").withIndex(60).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c060, ColumnMetadata.named("C060").withIndex(61).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c061, ColumnMetadata.named("C061").withIndex(62).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c062, ColumnMetadata.named("C062").withIndex(63).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c063, ColumnMetadata.named("C063").withIndex(64).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c064, ColumnMetadata.named("C064").withIndex(65).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(c065, ColumnMetadata.named("C065").withIndex(66).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(r, ColumnMetadata.named("R").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

