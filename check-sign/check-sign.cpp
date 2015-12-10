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

const LPCWSTR DLL_NAME = L"bicr4.dll";
HINSTANCE pDll = NULL;

// проверяет наличие файла
bool fileExists(const char *fname) {
  return access(fname, 0) != -1;
}

// возвращает текст ошибки по коду
char* getErrorMsg(int errorCode) {
	char *errorMsg = "";
	switch(errorCode) {
		case  1: return "ERR_MEM";
		case  2: return "ERR_BAD_SIGN";
		case  3: return "ERR_BAD_LEN";
		case  4: return "ERR_BAD_USER";
		case  5: return "ERR_CRYDRV";
		case  6: return "ERR_INIT_CRYMASK";
		case  8: return "ERR_NOT_SUPPORTED";
		case  9: return "ERR_CRC_SK_FILE";
		case 10: return "ERR_THREAD";
		case 11: return "ERR_NO_SIGN";
		case 12: return "ERR_OPEN_FILE";
		case 13: return "ERR_OPEN_MK_FILE";
		case 14: return "ERR_OPEN_PUB";
		case 15: return "ERR_OPEN_SK_FILE";
		case 17: return "ERR_READ_SK_FILE";
		case 18: return "ERR_READ_MK_FILE";
		case 19: return "ERR_SIGN_NO_REG";
		case 20: return "ERR_BAD_SELFTEST";
		case 21: return "ERR_GK_READ";
		case 22: return "ERR_UZ_READ";
		case 23: return "ERR_CRC_GKUZ";
		case 24: return "ERR_GKUZ_PSW";
		case 25: return "ERR_DSCH";
		case 26: return "ERR_CRC_TM";
		case 27: return "ERR_LOAD_GRN_DLL";
		case 28: return "ERR_STOP";
		case 29: return "ERR_TMDRV_NOT_FOUND";
		case 30: return "ERR_NO_TM_ATTACHED";
		case 31: return "ERR_READ_TM";
		case 32: return "ERR_BAD_PARAM";
		case 33: return "ERR_BAD_HANDLE";
		case 34: return "ERR_HANDLE_TYPE";
		case 35: return "ERR_WRITE_TM";
		case 37: return "ERR_READ_NET_FILE";
		case 39: return "ERR_INIT";
		case 40: return "ERR_LOAD_KEY";
		case 42: return "ERR_NET_KEY";
		case 43: return "ERR_NO_CRYP";
		case 44: return "ERR_BAD_CRYP";
		case 45: return "ERR_FILE_KEY";
		case 46: return "ERR_READ_FILE";
		case 47: return "ERR_WRITE_FILE";
		case 48: return "ERR_COMPRESS";
		case 49: return "ERR_MORE_DATA";
		default: return "";
	}
}

// инициализация
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

// загрузка БОК
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

// проверка ЭЦП
int checkFile(H_INIT h_init, H_PKBASE h_pkbase, const char *fileForCheck, char *sdelFlag) {
	int N = 1; //проверим первую ЭЦП
	int delFlag = strcmp(sdelFlag, "0"); // 1 - удаляем подпись, 0 - оставляем
	char user_ids[100];
	user_ids[0] = 0;
	int len_ids;
	len_ids = sizeof(user_ids);
	cr_check_file crСheckFile = (cr_check_file) GetProcAddress(pDll, "cr_check_file");
	int errorCode = crСheckFile(h_init, h_pkbase, fileForCheck, N, delFlag, user_ids, &len_ids);
	if (errorCode != 0) {
		printf("Result: FAIL\nERROR: cr_check_file, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

// закрытие БОК
int closeBase(H_PKBASE h_pkbase) {
	cr_pkbase_close crPkBaseClose = (cr_pkbase_close) GetProcAddress(pDll, "cr_pkbase_close");
	int errorCode = crPkBaseClose(h_pkbase);
	if (errorCode != 0) {
		printf("ERROR: cr_pkbase_close, %d %s\n", errorCode, getErrorMsg(errorCode));
	}
	return errorCode;
}

// деинициализация
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
			throw std::exception("ERROR: Incorrect argument count. Must be: BOK_FILE 0/1 CHECK_FILE");
		}
		char *sBokFile = argv[1];
		if (!fileExists(sBokFile)) {
			throw std::exception("ERROR: BOK-file must be exists");
		}
		char *sDelFlag = argv[2];
		if (strcmp(sDelFlag, "0") * strcmp(sDelFlag, "1") != 0) {
			throw std::exception("ERROR: Param \"del_flag\" must be \"0\" or \"1\"");
		}
		char *sFileForCheck = argv[3];
		if (!fileExists(sFileForCheck)) {
			throw std::exception("ERROR: File for check must be exists");
		}

		pDll = LoadLibrary(DLL_NAME); // загрузка библиотеки
		if (pDll == NULL) {
			throw std::exception("ERROR: Library not found");
		}
		int checkResult = 0;
		H_INIT h_init = init();// инициализация
		if (h_init != 0) {
			H_PKBASE h_pkbase = loadBase(h_init, sBokFile); // загрузка БОК
			if (h_pkbase != 0) {
				int checkResult = checkFile(h_init, h_pkbase, sFileForCheck, sDelFlag); // проверка ЭЦП
				if (checkResult == 0) {
					printf("Result: SUCCESS\n");
				}
				closeBase(h_pkbase); // закрываем БОК
			}
			uninit(h_init); // деинициализация
		}
	} catch (const std::exception& ex) {
		printf("Result: FAIL\n");
		printf(ex.what());
	} 
	// выгружаем библиотеку из памяти
	if (pDll != NULL) {
		FreeLibrary(pDll);
	}
}