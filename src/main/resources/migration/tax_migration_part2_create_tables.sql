--CREATE TABLES
--BASE OWNER = FACT
create table DEPARTMENT  (
   ID                   NUMBER                          not null,
   CODE                 VARCHAR2(20 char),
   DEFINITION           VARCHAR2(255 char),
   DATEOPEN             DATE,
   DATECLOSE            DATE,
   DATELOAD             DATE,
   TYPEDEP              VARCHAR2(50 char),
   PAR_FIELD            NUMBER,
   LEV_FIELD            NUMBER,
   TERBANK              VARCHAR2(3 char),
   BRANCH               VARCHAR2(4 char),
   SUBBRANCH            VARCHAR2(7 char),
   SHORTNAME            VARCHAR2(20 char)
);
   
create table NSI_BANK_RATE  (
   DATEOPEN             DATE,
   RATE                 NUMBER,
   NUMB_DOC             VARCHAR2(250 char),
   DATECLOSE            DATE,
   DATELOAD             DATE,
   FIDEXEMPLAR          NUMBER
);

create table NSI_CURRENCY  (
   ID                   NUMBER                          not null,
   ACTUAL               NUMBER                         default 0 not null,
   DATELOAD             DATE                           default SYSDATE,
   FIDEXEMPLAR          NUMBER                          not null,
   DATEOPEN             DATE,
   DATECLOSE            DATE,
   ISODIG               VARCHAR2(3 char),
   CODE                 VARCHAR2(3 char)                     not null,
   CBNAME               VARCHAR2(255 char),
   SBNAME               VARCHAR2(255 char)
);

create table NSI_DEPARTAX  (
   IDDEPARTAX           NUMBER                          not null,
   FIDDEPARTMENT        NUMBER,
   TB                   VARCHAR2(2 char)                     not null,
   BRANCH               VARCHAR2(4 char)                     not null,
   SUBBRANCH            VARCHAR2(2 char)                     not null,
   FLD                  VARCHAR2(1 char)                     not null,
   NAME                 VARCHAR2(80 char)                    not null,
   PARENT_TB            VARCHAR2(2 char)                     not null,
   PARENT_BRANCH        VARCHAR2(4 char)                     not null,
   PARENT_FLD           VARCHAR2(1 char)                     not null,
   FL_CONS              VARCHAR2(1 char)                     not null,
   FL_ACK               VARCHAR2(1 char)                     not null,
   DATEOPEN             DATE                            not null,
   DATECLOSE            DATE,
   FIDEXEMPLAR          NUMBER
);

create table NSI_OKATO  (
   ID_OKATO             NUMBER                          not null,
   DATELOAD             DATE                           default SYSDATE,
   FIDEXEMPLAR          NUMBER,
   TER                  VARCHAR2(3 char),
   KOD1                 VARCHAR2(3 char),
   KOD2                 VARCHAR2(3 char),
   KOD3                 VARCHAR2(3 char),
   RAZDEL               VARCHAR2(1 char),
   NAME1                VARCHAR2(200 char),
   CENTRUM              VARCHAR2(80 char),
   NOMAKT               NUMBER(15),
   TYPEAKT              VARCHAR2(2 char),
   DATAKT               DATE,
   KOD                  VARCHAR2(11 char)
);

create table NSI_RATES  (
   ID_RATES             NUMBER                          not null,
   DATELOAD             DATE                           default SYSDATE,
   FIDEXEMPLAR          NUMBER                          not null,
   H_TYPE_REC           NUMBER                          not null,
   H_NUM_ORDER          VARCHAR2(20 char),
   H_DATE_ORDER         DATE,
   H_DATE_BEGIN         DATE,
   H_DATE_END           DATE,
   B_TYPE_REC           NUMBER                          not null,
   B_ISO_CODE           VARCHAR2(3 char),
   B_SCALE              NUMBER,
   B_BUY                NUMBER,
   B_SALE               NUMBER
);

create table NSI_SECURITY  (
   ID                   NUMBER                          not null,
   DATELOAD             DATE                           default SYSDATE not null,
   FIDEXEMPLAR          NUMBER                          not null,
   SECURITY_ID          NUMBER                          not null,
   INSTITUTION_ID       NUMBER,
   CURRENCY_NUM         VARCHAR2(3 char),
   FULL_NAME            VARCHAR2(255 char),
   BRIEF_NAME           VARCHAR2(25 char),
   REG_NUM              VARCHAR2(30 char),
   VOLUME               NUMBER,
   SECUR_TYPE           VARCHAR2(1 char),
   DATEBEGIN            DATE,
   DATEEND              DATE
);

--BASE OWNER = COMMON
create table APP  (
   IDAPP                NUMBER                          not null,
   APP_NAME             VARCHAR2(30 char)                    not null,
   APP_PWD              VARCHAR2(20 char),
   APP_DEF              VARCHAR2(200 char),
   APP_LOGIN            VARCHAR2(20 char),
   APP_APT_ID           NUMBER,
   APP_PASSWD           VARCHAR2(20 char),
   APP_UNICODE          VARCHAR2(10 char),
   APP_EXCLUSE          NUMBER,
   APP_AST_ID           NUMBER,
   APP_CHECK            NUMBER,
   APP_CRC              NUMBER,
   APP_CLASS            VARCHAR2(100 char),
   APP_CAPTION          VARCHAR2(100 char)
);

create table ROLES  (
   RL_ID                NUMBER                          not null,
   RL_APP_ID            NUMBER,
   RL_ORANAME           VARCHAR2(30 char),
   RL_NAME              VARCHAR2(30 char),
   RL_UNICODE           VARCHAR2(20 char),
   RL_EXCL_ID           NUMBER,
   RL_CANCHECK          NUMBER,
   RL_GROUP             VARCHAR2(30 char)
);

create table SEVERITIES  (
   ID                   VARCHAR2(20 char)               not null,
   NAME                 VARCHAR2(50 char)               not null
);

create table USEREVENT  (
   UE_ID                NUMBER                          not null,
   UE_APP_ID            NUMBER                          not null,
   UE_NAME              VARCHAR2(200 char),
   UE_CODE              VARCHAR2(50 char)
);

create table USERLOG  (
   UL_AUTHOR            NUMBER,
   UL_USR_ID            NUMBER,
   UL_DATE              DATE,
   UL_COMMENT           VARCHAR2(4000 char),
   UL_UE_ID             NUMBER,
   UL_RL_ID             NUMBER,
   UL_IP                VARCHAR2(20 char),
   UL_MARK              NUMBER,
   UL_SEVERITY          VARCHAR2(1 char),
   UL_STATUS            NUMBER
);

--BASE OWNER=FACT
create table ACSLEVEL  (
   IDACSLEVEL           NUMBER                          not null,
   DEFINITION           VARCHAR2(100 char)
);

create table ASYSTEM  (
   IDASYSTEM            NUMBER                          not null,
   CODEASYSTEM          VARCHAR2(20 char)                    not null,
   DEFINITION           VARCHAR2(255 char)                   not null,
   DATEOPEN             DATE,
   DATECLOSE            DATE,
   DATELOAD             DATE,
   FIDASYSTEM           NUMBER,
   CODESUBSYSTEM        VARCHAR2(2 char)
);

create table DOP_TRAN  (
   IDDOPTRAN            NUMBER                          not null,
   FIDTRAN              NUMBER,
   FIDEVENT             NUMBER                          not null,
   THEDATETIME          DATE,
   COMMENTS             VARCHAR2(4000 char),
   FIDOBJ               NUMBER,
   FIDPERIODLIST        NUMBER
);

create table ENTRANCEDIR  (
   IDENTRANCEDIR        NUMBER                          not null,
   IPADDRESS            VARCHAR2(15 char),
   RECEIVEDIR           VARCHAR2(80 char),
   PORT             	NUMBER(4),
   TYPEENTRDIR    	  	VARCHAR2(4 char),
   FLAGPROCESSING 	  	VARCHAR2(1 char),
   FLAGVACANCY    	  	VARCHAR2(1 char),
   FLAGCLEAR      	  	VARCHAR2(1 char),
   FLAGWRECK        	VARCHAR2(1 char),
   RECWEB           	VARCHAR2(200 char),
   SENDDIR              VARCHAR2(80 char),
   FLAGBLOCKADE         VARCHAR2(1 char),
   SENDWEB              VARCHAR2(200 char),
   KIND                 NUMBER                         default NULL,
   SERVERNAME           VARCHAR2(100 char),
   QUEUENAME            VARCHAR2(100 char),
   FTIME                VARCHAR2(10 char),
   LTIME                VARCHAR2(10 char),
   FIDDEPARTMENT        NUMBER
);

create table EVENTGROUP  (
   IDEVENTGROUP         NUMBER                          not null,
   DEFINITION           VARCHAR2(200 char)
);

create table EVENTS  (
   IDEVENT              NUMBER                          not null,
   FIDACSLEVEL          NUMBER,
   DEFINITION           VARCHAR2(250 char)                   not null,
   CODEEVENT            VARCHAR2(30 char),
   GUIDELINE            VARCHAR2(250 char),
   HELP                 VARCHAR2(2000 char),
   FIDEVENTGROUP        NUMBER
);


create table EXEMPLAR  (
   IDEXEMPLAR           NUMBER                          not null,
   FIDOBJ               NUMBER                          not null,
   FIDPERIODLIST        NUMBER,
   FIDTRANSACTION       NUMBER,
   TYPEEXEMPLAR         VARCHAR2(8 char),
   TYPELOAD             VARCHAR2(8 char),
   FLAGACTUALEXEMPLAR   VARCHAR2(1 char),
   FLAGBLOCK            VARCHAR2(1 char),
   SIGNATURE            VARCHAR2(200 char),
   DATELOAD             DATE,
   ERRCODE              NUMBER,
   ERRDESC              VARCHAR2(255 char),
   LARGE_MESS           VARCHAR2(2499 char),
   FIDFILE              NUMBER,
   EX_FILENAME          VARCHAR2(50 char),
   FIDVEROBJ            NUMBER,
   KEYSECTION           VARCHAR2(50 char),
   PARENTEXEMPLAR       NUMBER,
   TYPEBLOCK            NUMBER,
   FLAGCALC             NUMBER,
   BATCHNUMBER          VARCHAR2(20 char)
);

create table GENPARAM  (
   IDGENPARAM           NUMBER,
   FIDSECTION           NUMBER,
   SECTIONORDER         NUMBER,
   CHAR_VAL             VARCHAR2(2000 char),
   DATE_VAL             DATE,
   NUM_VAL              NUMBER,
   TEXT                 VARCHAR2(50 char),
   INI                  NUMBER,
   INISECTION           VARCHAR2(20 char),
   INICODE              VARCHAR2(50 char),
   PRM_KIND             VARCHAR2(10 char),
   GP_ID                NUMBER
);

create table IN_FILE  (
   FIDVERLOAD           NUMBER,
   MASKINFILE           VARCHAR2(30 char),
   CODEINFILE           VARCHAR2(20 char),
   DELIMFIELD           VARCHAR2(1 char),
   FLAGREQFILE          VARCHAR2(50 char),
   FLAGBELMON           VARCHAR2(3 char),
   FLAGBELLOAD          VARCHAR2(3 char),
   TABNAME              VARCHAR2(50 char),
   IDFILE               NUMBER                          not null,
   FIDPROCESS           NUMBER,
   FIDSUBDICT           NUMBER,
   DEFINITION           VARCHAR2(250 char),
   NUM_POS              NUMBER,
   FILEORDER            NUMBER,
   IS_STORN             NUMBER,
   IDINFILE             NUMBER,
   ENCODING             VARCHAR2(20 char),
   FIDTABLE             NUMBER,
   DIRECTLOAD           NUMBER
);

create table OBJ  (
   IDOBJ                NUMBER                          not null,
   FIDOBJDICT           NUMBER,
   FIDVEROBJ            NUMBER,
   FIDPERIODITY         NUMBER,
   FIDSTREAM            NUMBER,
   FIDPROVIDER          NUMBER,
   DATELOAD             DATE,
   DATEOPEN             DATE,
   DATECLOSE            DATE,
   SIGNCOUNT            NUMBER,
   TYPEFIRSTSEC         VARCHAR2(8 char),
   TYPEINOUT            VARCHAR2(8 char),
   TIMEOF               VARCHAR2(8 char),
   FLAGINSTREAM         VARCHAR2(1 char),
   FLAGREWRITE          VARCHAR2(1 char),
   FLAGBLOCK            VARCHAR2(1 char),
   OBJ_TYPE             VARCHAR2(20 char),
   SHORT_DEF            VARCHAR2(20 char),
   FIDSUBDICT           NUMBER,
   FIDSHAPE             NUMBER,
   FLAGCLEAN            NUMBER,
   OBJ_FLAGS            NUMBER,
   FIDOBJHIST           NUMBER
);

create table OBJDICT  (
   IDOBJDICT            NUMBER                          not null,
   DATELOAD             DATE,
   CODEOBJDICT          VARCHAR2(120 char),
   DEFINITION           VARCHAR2(255 char)                   not null,
   FLAGBLOCK            VARCHAR2(1 char),
   OWNER                VARCHAR2(20 char),
   TYPEOBJDICT          VARCHAR2(8 char),
   FLAGNEWROW           VARCHAR2(1 char),
   DATEOPEN             DATE,
   DATECLOSE            DATE
);

create table PERIODITY  (
   IDPERIODITY          NUMBER                          not null,
   DATELOAD             DATE,
   CODEPERIODITY        VARCHAR2(20 char)                    not null,
   DEFINITION           VARCHAR2(255 char)                   not null,
   FLAGNEWROW           VARCHAR2(1 char),
   FLAGHAVELIST         VARCHAR2(1 char),
   DATEOPEN             DATE,
   DATECLOSE            DATE
);


create table PERIODLIST  (
   IDPERIODLIST         NUMBER                          not null,
   CODEPERIODLIST       VARCHAR2(20 char)                    not null,
   DEFINITION           VARCHAR2(255 char),
   FCODEPERIODITY       VARCHAR2(20 char),
   DATEBEGIN            DATE,
   DATEEND              DATE,
   DATELOAD             DATE,
   FIDPERIODITY         NUMBER
);

create table PRELIMPROC  (
   IDPRELIMPROC         NUMBER                          not null,
   IDENTRANCEDIR        NUMBER,
   FILEMASK             VARCHAR2(100 char)                   not null,
   FLAGBLOCKADEPP       VARCHAR2(1 char),
   PREFERENCE           NUMBER,
   WAY                  VARCHAR2(10 char),
   TYPECIPHER           VARCHAR2(15 char),
   FUNCCIPHER           VARCHAR2(15 char),
   TYPECOMPRESS         VARCHAR2(15 char),
   FUNCCOMPRESS         VARCHAR2(15 char),
   TYPEPACKING          VARCHAR2(15 char),
   FUNCPACKING          VARCHAR2(15 char),
   TYPEPRESENTATION     VARCHAR2(15 char),
   FUNCPRESENTATION     VARCHAR2(15 char),
   TYPESIGNATURE        VARCHAR2(15 char),
   FUNCSIGNATURE        VARCHAR2(15 char),
   QUANTITYSIGN         NUMBER(4),
   SEEKOBJ              VARCHAR2(80 char),
   DATESTART            DATE,
   DATEEND              DATE,
   DEFINITION           VARCHAR2(200 char),
   FILEQUITTANCEMASK    VARCHAR2(15 char),
   IDOBJDICT            NUMBER(9),
   IDPERIODITY          NUMBER(9),
   IDPROVIDER           NUMBER(9),
   IDSHAPE              NUMBER(9),
   DEFPROVIDER          VARCHAR2(255 char),
   FIDSUBDICT           NUMBER,
   FIDPROVIDER          NUMBER,
   FIDPERIODITY         NUMBER,
   FIDCONVERSE          NUMBER,
   FIDWAY               NUMBER,
   FIDENTRANCEDIR       NUMBER,
   FIDSTREAM            NUMBER,
   MONITOR_VERSION      NUMBER(5)                      default 1
   );

create table PROVIDER  (
   IDPROVIDER           NUMBER                          not null,
   DATELOAD             DATE,
   CODEPROVIDER         VARCHAR2(20 char),
   DEFINITION           VARCHAR2(255 char),
   TYPEPROVIDER         VARCHAR2(8 char),
   TYPEINOUT            VARCHAR2(8 char),
   TYPEGROUP            VARCHAR2(8 char),
   PARENT               NUMBER,
   FLAGNEWROW           VARCHAR2(1 char),
   DATEOPEN             DATE,
   DATECLOSE            DATE,
   FIDASYSTEM           NUMBER,
   FIDDEPARTMENT        NUMBER
);

   
create table SUBDICT  (
   IDSUBDICT            NUMBER                          not null,
   FIDOBJDICT           NUMBER,
   CODESUBDICT          VARCHAR2(200 char),
   DEFINITION           VARCHAR2(255 char),
   TYPESUBDICT          VARCHAR2(8 char)
);

create table TRANSACTION  (
   IDTRANSACTION        NUMBER                          not null,
   IDMONITOR            NUMBER,
   IDPRELIMPROC         NUMBER,
   IDENTRANCEDIR        NUMBER,
   FILENAME             VARCHAR2(100 char),
   TIMESTART            DATE,
   FLAGLOADEND          VARCHAR2(1 char),
   ENDCODE              NUMBER,
   ENDDESC              VARCHAR2(255 char),
   FLAGOUT              VARCHAR2(1 char),
   FLAGMONJOB           VARCHAR2(1 char),
   DEFPROVIDER          VARCHAR2(80 char),
   DEFPRELIM            VARCHAR2(80 char),
   REPDATE              DATE,
   SIGNTXT              VARCHAR2(255 char),
   FIDMONITOR           NUMBER,
   FIDPRELIMPROC        NUMBER,
   ARCHDIR              VARCHAR2(250 char),
   FILEDATE             DATE,
   FIDEXEMPLAR          NUMBER,
   FIDDOPTRAN           NUMBER,
   FIDPROVIDER          NUMBER
);

create table VEROBJ  (
   IDVEROBJ             NUMBER                          not null,
   DEFINITION           VARCHAR2(255 char)                   not null,
   DESCRIPTION          VARCHAR2(400 char),
   CODE                 VARCHAR2(20 char),
   DATELOAD             DATE                            not null
);

create table WAY  (
   IDWAY                NUMBER                          not null,
   FIDPROCESS           NUMBER,
   DEFINITION           VARCHAR2(200 char),
   KIND                 VARCHAR2(10 char)
);

--BASE OWNER = TR
create table TRD_25  (
   NUMSTATE             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1 char),
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000  char)
);

create table TRD_25M  (
   NUMSTATE             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1 char),
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

create table TRD_26  (
   EMITTER              VARCHAR2(255 char),
   TYPESHARES           VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   CURSHARES            VARCHAR2(10 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1 char),
   QUOTCUR              NUMBER,
   RATEQUOT             NUMBER,
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

create table TRD_26M  (
   EMITTER              VARCHAR2(255 char),
   TYPESHARES           VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   CURSHARES            VARCHAR2(10 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1 char),
   QUOTCUR              NUMBER,
   RATEQUOT             NUMBER,
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

create table TRD_27  (
   EMITTER              VARCHAR2(255 char),
   NUMSTATE             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   CURSHARES            VARCHAR2(10 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1 char),
   QUOTCUR              NUMBER,
   RATEQUOT             NUMBER,
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

create table TRD_27M  (
   EMITTER              VARCHAR2(255 char),
   NUMSTATE             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   CURSHARES            VARCHAR2(10 char),
   NUMPAPERPREV         NUMBER(15,2),
   NUMPAPER             NUMBER(15,2),
   RESERVEPREV          NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   TYPEPAPER            VARCHAR2(1),
   QUOTCUR              NUMBER,
   RATEQUOT             NUMBER,
   MARKETPRICEONE       NUMBER,
   MARKETPRICE          NUMBER(15,2),
   RESERVE              NUMBER(15,2),
   RESERVECREATE        NUMBER(15,2),
   RESERVEREST          NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
   );

create table TRD_31  (
   CODETYPEPAPER        NUMBER(15),
   TYPEPAPER            VARCHAR2(255 char),
   PERCCASHOFZ          NUMBER,
   PERCCASHEURO         NUMBER,
   PERCCASHFED          NUMBER,
   PERCCASHOGVZ         NUMBER,
   PERCCASHOTHER        NUMBER,
   PERCCASHCORP         NUMBER,
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   PERCCASHIPOTAFTER    NUMBER,
   PERCCASHMUN          NUMBER,
   PERCCASHIPOTBEFORE   NUMBER,
   PERCCASHEURONEW      NUMBER,
   PERCCASHBEL          NUMBER
);

create table TRD_31M  (
   CODETYPEPAPER        NUMBER(15),
   TYPEPAPER            VARCHAR2(255 char),
   PERCCASHOFZ          NUMBER,
   PERCCASHEURO         NUMBER,
   PERCCASHFED          NUMBER,
   PERCCASHOGVZ         NUMBER,
   PERCCASHOTHER        NUMBER,
   PERCCASHCORP         NUMBER,
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   PERCCASHIPOTAFTER    NUMBER,
   PERCCASHMUN          NUMBER,
   PERCCASHIPOTBEFORE   NUMBER,
   PERCCASHEURONEW      NUMBER,
   PERCCASHBEL          NUMBER
   );

create table TRD_51  (
   CODEDEAL             NUMBER(1),
   TYPEPAPER            VARCHAR2(1 char),
   DEFPAPER             VARCHAR2(255 char),
   DIMPL                DATE,
   NUMPAPER             NUMBER(15),
   RGETPRICE            NUMBER(15,2),
   RGETCOST             NUMBER(15,2),
   RSUMEXT              NUMBER(15,2),
   SALEPRICEPERC        NUMBER,
   RSALEPRICE           NUMBER(15,2),
   MARKETPRICEPERC      NUMBER,
   RMARKETPRICE         NUMBER(15,2),
   RSALEPRICETAX        NUMBER(15,2),
   RCOST                NUMBER(15,2),
   RTOTALCOST           NUMBER(15,2),
   RPROFITCOST          NUMBER(15,2),
   ROVWRPRICE           NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   DGET                 DATE,
   GETMPRICEPERC        NUMBER(15,4),
   GETMPRICE            NUMBER(15,2),
   GETSALEPRICETAX      NUMBER(15,2)
);

create table TRD_51M  (
   CODEDEAL             NUMBER(1),
   TYPEPAPER            VARCHAR2(1 char),
   DEFPAPER             VARCHAR2(255 char),
   DIMPL                DATE,
   NUMPAPER             NUMBER(15),
   RGETPRICE            NUMBER(15,2),
   RGETCOST             NUMBER(15,2),
   RSUMEXT              NUMBER(15,2),
   SALEPRICEPERC        NUMBER,
   RSALEPRICE           NUMBER(15,2),
   MARKETPRICEPERC      NUMBER,
   RMARKETPRICE         NUMBER(15,2),
   RSALEPRICETAX        NUMBER(15,2),
   RCOST                NUMBER(15,2),
   RTOTALCOST           NUMBER(15,2),
   RPROFITCOST          NUMBER(15,2),
   ROVWRPRICE           NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   DGET                 DATE,
   GETMPRICEPERC        NUMBER(15,4),
   GETMPRICE            NUMBER(15,2),
   GETSALEPRICETAX      NUMBER(15,2)
);

create table TRD_53  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   CODECURRENCY         VARCHAR2(10 char),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   GETPRICE             NUMBER(15,2),
   SALEPRICE            NUMBER(15,2),
   DIFFPRICE            NUMBER(15,2),
   RSUMIMPL             NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_53M  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   CODECURRENCY         VARCHAR2(10 char),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   GETPRICE             NUMBER(15,2),
   SALEPRICE            NUMBER(15,2),
   DIFFPRICE            NUMBER(15,2),
   RSUMIMPL             NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
   );

create table TRD_54  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   CODECURRENCY         VARCHAR2(10 char),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   SALEPRICE            NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   DIFFPRICE            NUMBER(15,2),
   RSUMIMPL             NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_54M  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   CODECURRENCY         VARCHAR2(10 char),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   SALEPRICE            NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   DIFFPRICE            NUMBER(15,2),
   RSUMIMPL             NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_59  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   NUMPAPER2            NUMBER(9),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   DREPO2FACT           DATE,
   GETPRICE             NUMBER(15,2),
   SALEPRICE            NUMBER(15,2),
   CODECURRENCY         VARCHAR2(10 char),
   RATE                 NUMBER(15,4),
   RRESREPO             NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_59M  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   NUMPAPER2            NUMBER(9),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   DREPO2FACT           DATE,
   GETPRICE             NUMBER(15,2),
   SALEPRICE            NUMBER(15,2),
   CODECURRENCY         VARCHAR2(10 char),
   RATE                 NUMBER(15,4),
   RRESREPO             NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);


create table TRD_60  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   NUMPAPER2            NUMBER(9),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   DREPO2FACT           DATE,
   SALEPRICE            NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   CODECURRENCY         VARCHAR2(10 char),
   RRESREPO             NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_60M  (
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   NUMPAPER1            NUMBER(9),
   NUMPAPER2            NUMBER(9),
   NOMPAPER             NUMBER(15,2),
   DREPO1               DATE,
   DREPO2               DATE,
   DREPO2FACT           DATE,
   SALEPRICE            NUMBER(15,2),
   GETPRICE             NUMBER(15,2),
   CODECURRENCY         VARCHAR2(10 char),
   RRESREPO             NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char),
   GETPRICENKD          NUMBER(15,2),
   SALEPRICENKD         NUMBER(15,2),
   IMPLREPO             NUMBER(15,2),
   COSTREPO             NUMBER(15,2),
   BANKRATE             NUMBER,
   COSTREPO269          NUMBER(15,2),
   COSTREPOTAX          NUMBER(15,2)
);

create table TRD_64  (
   DDEAL                DATE,
   PARTDEAL             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   RCOST                NUMBER(15,2),
   FIDEXEMPLAR          NUMBER(9)                       not null,
   NEWNUM               NUMBER(9),
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

create table TRD_64M  (
   DDEAL                DATE,
   PARTDEAL             VARCHAR2(255 char),
   NUMDEAL              VARCHAR2(255 char),
   DEFPAPER             VARCHAR2(255 char),
   RCOST                NUMBER(15,2),
   FIDMNLEXEMPLAR       NUMBER(9)                       not null,
   UPDATOR              VARCHAR2(30 char),
   UPDDATE              DATE,
   NUM                  NUMBER(9),
   FLAGFIX              VARCHAR2(8 char),
   CREATOR              VARCHAR2(30 char),
   CREDATE              DATE,
   TYPEROW              VARCHAR2(8 char),
   IDROW                NUMBER(9)                       not null,
   ERRTEXT              VARCHAR2(4000 char),
   ERRTYPE              VARCHAR2(8 char),
   ERRPACK              VARCHAR2(4000 char)
);

