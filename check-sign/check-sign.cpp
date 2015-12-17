#include <exception>
#include <io.h>
#include <stdio.h>
#include <windows.h>

typedef int BICR_HANDLE;
typedef BICR_HANDLE H_INIT;
typedef BICR_HANDLE H_PKBASE;

typedef int (__stdcall *cr_init) (int tm_flag, const char *gk, const char *uz, const char *psw, void *tm_number, int *tmn_blen, int *init_mode, H_INIT *init_handle);
typedef int (__stdcall *cr_pkbase_load) (H_INIT init_handle, const char *pk_file, int com_blen, int flag_modify, H_PKBASE* pkbase_handle);
typedef int (__stdcall *cr_check_file) (H_INIT init_handle, H_PKBASE pkbase_handle, const char *file_name, int N, int flag_del, char *userid, int *userid_blen);
typedef int (__stdcall *cr_pkbase_close) (H_PKBASE pkbase_handle);
typedef int (__stdcall *cr_uninit) (H_INIT init_handle);

const LPCSTR DLL_NAME = "bicr4.dll";
HINSTANCE pDll = NULL;

// яЁютхЁ хЄ эрышўшх Їрщыр
bool fileExists(const char *fname) {
  return _access(fname, 0) != -1;
}

// тючтЁр∙рхЄ ЄхъёЄ ю°шсъш яю ъюфє
char* getErrorMsg(int errorCode) {
	char *errorMsg = "";
	switch(errorCode) {
		case  1: return "ERR_MEM, Недостаточно памяти";
		case  2: return "ERR_BAD_SIGN, Подпись неверна";
		case  3: return "ERR_BAD_LEN, Длина буфера неверна";
		case  4: return "ERR_BAD_USER, Номер пользователя неверен";
		case  5: return "ERR_CRYDRV, Ошибка средств шифрования";
		case  6: return "ERR_INIT_CRYMASK, Ошибка декодирования мастер-ключа";
		case  8: return "ERR_NOT_SUPPORTED, Используются функции из КСБ-С, которых нет в 4.0";
		case  9: return "ERR_CRC_SK_FILE, Ошибка контрольной суммы файла с закрытым ключом";
		case 10: return "ERR_THREAD, Ошибка многопоточности";
		case 11: return "ERR_NO_SIGN, В проверяемом файле отсутствует подпись";
		case 12: return "ERR_OPEN_FILE, Ошибка открытия файла";
		case 13: return "ERR_OPEN_MK_FILE, Ошибка открытия файла с мастер ключом ";
		case 14: return "ERR_OPEN_PUB, Ошибка открытия файла с публичным ключом";
		case 15: return "ERR_OPEN_SK_FILE, Ошибка открытия файла с закрытым ключом";
		case 17: return "ERR_READ_SK_FILE, Ошибка чтения файла с закрытым ключом";
		case 18: return "ERR_READ_MK_FILE, Ошибка чтения файла с мастер-ключом";
		case 19: return "ERR_SIGN_NO_REG, Идентификатор подписи не зарегистрирован в БОК";
		case 20: return "ERR_BAD_SELFTEST, Внутренние тесты библиотеки проведены с ошибкой";
		case 21: return "ERR_GK_READ, Ошибка чтения главного ключа";
		case 22: return "ERR_UZ_READ, Ошибка чтения узла замены";
		case 23: return "ERR_CRC_GKUZ, Ошибка контрольной суммы главного ключа";
		case 24: return "ERR_GKUZ_PSW, Главный ключ требует ввода пароля";
		case 25: return "ERR_DSCH, Не найден датчик ДСЧ ";
		case 26: return "ERR_CRC_TM, Ошибка контрольной суммы при чтении ТМ";
		case 27: return "ERR_LOAD_GRN_DLL, Ошибка загрузки библиотеки grn.dll";
		case 28: return "ERR_STOP, Остановлено пользователем";
		case 29: return "ERR_TMDRV_NOT_FOUND, Не драйвера ТМ";
		case 30: return "ERR_NO_TM_ATTACHED, Не приложена ТМ к съемнику";
		case 31: return "ERR_READ_TM, Ошибка чтения TM";
		case 32: return "ERR_BAD_PARAM, Ошибка в параметрах функции";
		case 33: return "ERR_BAD_HANDLE, Ошибка дескриптора (например, происходит обращение к закрытому ранее дескриптору или вместо валидного дескриптора используется случайное значение)";
		case 34: return "ERR_HANDLE_TYPE, Неправильный тип дескриптора (например, вместо типа H_USER в соответствующий параметр функции передается дескриптор типа H_PKEY)";
		case 35: return "ERR_WRITE_TM, Ошибка записи ТМ";
		case 37: return "ERR_READ_NET_FILE, Ошибка чтения файла сетевых ключей";
		case 39: return "ERR_INIT, Ошибка инициализации библиотеки, не был вызван cr_init";
		case 40: return "ERR_LOAD_KEY, Ошибка загрузки ключа";
		case 42: return "ERR_NET_KEY, Ошибка сетевого ключа";
		case 43: return "ERR_NO_CRYP, Буфер не был зашифрован";
		case 44: return "ERR_BAD_CRYP, Ошибка расшифрования буфера";
		case 45: return "ERR_FILE_KEY, Ошибка файлового ключа";
		case 46: return "ERR_READ_FILE, Ошибка чтения файла";
		case 47: return "ERR_WRITE_FILE, Ошибка записи файла";
		case 48: return "ERR_COMPRESS, Ошибка компрессии";
		case 49: return "ERR_MORE_DATA, Ошибка - длина буфера недостаточна";
		default: return "";
	}
}

// шэшЎшрышчрЎш 
H_INIT init() {
	int USE_TM = 0;
	char *FILE_GK = "";
	char *FILE_UZ = "";
	int mode;
	H_INIT h_init = 0;
	cr_init crInit = (cr_init) GetProcAddress(pDll, "cr_init");
	crInit(USE_TM + 128, FILE_GK, FILE_UZ, "", NULL, NULL, &mode, &h_init);
	if (h_init == 0) {
		printf("Result: FAIL\nERROR: Not initialized\n");
	}
	return h_init;
}

// чруЁєчър ┴╬╩
H_PKBASE loadBase(H_INIT h_init, const char *bokFile) {
	int COM_LEN = 0;
	H_PKBASE h_pkbase = 0;
	cr_pkbase_load crPkBaseLoad = (cr_pkbase_load) GetProcAddress(pDll, "cr_pkbase_load");
	int errorCode = crPkBaseLoad(h_init, bokFile, COM_LEN, 0, &h_pkbase);
	if (errorCode != 0) {
		printf("Result: FAIL\nERROR: cr_pkbase_load, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return h_pkbase;
}

// яЁютхЁър ▌╓╧
int checkFile(H_INIT h_init, H_PKBASE h_pkbase, const char *fileForCheck, char *sdelFlag) {
	int N = 1; //яЁютхЁшь яхЁтє■ ▌╓╧
	int delFlag = strcmp(sdelFlag, "0"); // 1 - єфры хь яюфяшё№, 0 - юёЄрты хь
	char user_ids[100];
	user_ids[0] = 0;
	int len_ids;
	len_ids = sizeof(user_ids);
	cr_check_file cr╤heckFile = (cr_check_file) GetProcAddress(pDll, "cr_check_file");
	int errorCode = cr╤heckFile(h_init, h_pkbase, fileForCheck, N, delFlag, user_ids, &len_ids);
	if (errorCode != 0) {
		printf("Result: FAIL\nERROR: cr_check_file, %d %s\n", errorCode, getErrorMsg(errorCode));
	} else {
		printf("Result: SUCCESS\n");
		printf("UserId: %s\n", user_ids);
	}
	return errorCode;
}

// чръЁ√Єшх ┴╬╩
int closeBase(H_PKBASE h_pkbase) {
	cr_pkbase_close crPkBaseClose = (cr_pkbase_close) GetProcAddress(pDll, "cr_pkbase_close");
	int errorCode = crPkBaseClose(h_pkbase);
	if (errorCode != 0) {
		printf("ERROR: cr_pkbase_close, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

// фхшэшЎшрышчрЎш 
int uninit(H_INIT h_init) {
	cr_uninit crUninit = (cr_uninit) GetProcAddress(pDll, "cr_uninit");
	int errorCode = crUninit(h_init);
	if (errorCode != 0) {
		printf("ERROR: cr_pkbase_close, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

int main(int argc, char **argv) {
	try {
		if (argc != 4) {
			throw std::exception("ERROR: Incorrect argument count. Must be: BOK_FILE 0/1 CHECK_FILE\n");
		}
		char *sBokFile = argv[1];
		if (!fileExists(sBokFile)) {
			throw std::exception("ERROR: BOK-file must be exists\n");
		}
		char *sDelFlag = argv[2];
		if (strcmp(sDelFlag, "0") * strcmp(sDelFlag, "1") != 0) {
			throw std::exception("ERROR: Param \"del_flag\" must be \"0\" or \"1\"\n");
		}
		char *sFileForCheck = argv[3];
		if (!fileExists(sFileForCheck)) {
			throw std::exception("ERROR: File for check must be exists\n");
		}

		pDll = LoadLibrary(DLL_NAME); // чруЁєчър сшсышюЄхъш
		if (pDll == NULL) {
			throw std::exception("ERROR: Library not found\n");
		}
		int checkResult = 0;
		H_INIT h_init = init();// шэшЎшрышчрЎш 
		if (h_init != 0) {
			H_PKBASE h_pkbase = loadBase(h_init, sBokFile); // чруЁєчър ┴╬╩
			if (h_pkbase != 0) {
				checkFile(h_init, h_pkbase, sFileForCheck, sDelFlag); // яЁютхЁър ▌╓╧
				closeBase(h_pkbase); // чръЁ√трхь ┴╬╩
			}
			uninit(h_init); // фхшэшЎшрышчрЎш 
		}
	} catch (const std::exception& ex) {
		printf("Result: FAIL\n");
		printf(ex.what());
	} 
	// т√уЁєцрхь сшсышюЄхъє шч ярь Єш
	if (pDll != NULL) {
		FreeLibrary(pDll);
	}
}