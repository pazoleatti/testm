using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;

class CheckSign {
	
	[UnmanagedFunctionPointer(CallingConvention.Cdecl)]
	private delegate int CrInit(int var0, String var1, String var2, String var3, byte[] var4, int[] var5, int[] var6, long[] var7);
	[UnmanagedFunctionPointer(CallingConvention.Cdecl)]
	private delegate int CrPkBaseLoad(long var0, String var2, int var3, int var4, long[] var5);
	[UnmanagedFunctionPointer(CallingConvention.Cdecl)]
	private delegate int CrCheckFile(long var0, long var2, String var4, int var5, int var6, StringBuilder var7);
	[UnmanagedFunctionPointer(CallingConvention.Cdecl)]
	private delegate int CrPkBaseClose(long var0);
	[UnmanagedFunctionPointer(CallingConvention.Cdecl)]
	private delegate int CrUninit(long var0);
	
	/** Название библиотеки для проверки подписи*/	
	private static string DLL_NAME = "bicr4_64.dll";
		
	static void Main(string[] args) {
		Stopwatch pSw = new Stopwatch();
		pSw.Start();
		try {
			if (args.Length != 4) {
				throw new Exception (
					"Program description: The file signature verification\n" +
					"Usage syntax: CheckSign.exe \"PATH_TO_KEY_FILE\" \"PATH_TO_DLL_FOLDER\" \"DEL_FLAG(0/1)\" \"FILE_FOR_CHECK\"\n" +
					"Result: FAIL or SUCCESS will be written in system out.");
			}
			// считывание и проверка аргументов
			string keyFleName = args[0];
			if(!File.Exists(keyFleName)) {
				throw new Exception ("ERROR: \"PATH_TO_KEY_FILE\" param must be a file: " + keyFleName);
			}
			string dllFolder = args[1];
			if(!Directory.Exists(dllFolder)) {
				throw new Exception ("ERROR: \"PATH_TO_DLL_FOLDER\" param must be a folder: " + dllFolder);
			}
			string delFlag = args[2];
			if(delFlag != "1" && delFlag != "0") {
				throw new Exception ("ERROR: \"DEL_FLAG\" param must be 1 or 0: " + delFlag);
			}
			string checkFileName = args[3];
			if(!File.Exists(checkFileName)) {
				throw new Exception ("ERROR: \"FILE_FOR_CHECK\" param must be a file: " + checkFileName);
			}
			// загружаем библиотеку
			IntPtr pDll = IntPtr.Zero;
			try {
				pDll = NativeMethods.LoadLibrary(dllFolder + "/" + DLL_NAME);
				if (pDll == IntPtr.Zero) {
					throw new Exception ("Library \"" + DLL_NAME + "\" not found");
				}
				// инициализация
				IntPtr pCrInit = loadMethod(pDll, "cr_init");
				CrInit crInit = (CrInit) Marshal.GetDelegateForFunctionPointer(pCrInit, typeof(CrInit));
				int FLAG_TM = 128;
				string FILE_GK = "";
				string FILE_UZ = "";
				string PSW = "";
				byte[] TM_NUMBER = new byte[32];
				int[] TMN_BLEN = new int[1];
				TMN_BLEN[0] = 32;
				int[] initMode = new int[10];
				long[] initStruct = new long[10];
				crInit(FLAG_TM, FILE_GK, FILE_UZ, PSW, TM_NUMBER, TMN_BLEN, initMode, initStruct);
				int errorCode = 0;
				try {
					// загрузка БОК
					IntPtr pСrPkBaseLoad = loadMethod(pDll, "cr_pkbase_load");
					CrPkBaseLoad crPkBaseLoad = (CrPkBaseLoad) Marshal.GetDelegateForFunctionPointer(pСrPkBaseLoad, typeof(CrPkBaseLoad));
					int COM_LEN = 0;
					long[] pkBaseStruct  = new long[10];
					errorCode =  crPkBaseLoad(initStruct[0], keyFleName, COM_LEN, 0, pkBaseStruct);
					CheckError("cr_pkbase_load", errorCode);
					try {
						// проверка ЭЦП
						IntPtr pCrCheckFile = loadMethod(pDll, "cr_check_file");
						CrCheckFile crCheckFile = (CrCheckFile) Marshal.GetDelegateForFunctionPointer(pCrCheckFile, typeof(CrCheckFile));
						int n = 1; //проверим первую ЭЦП
						StringBuilder userIdBuf = new StringBuilder("6"); // магическое число
						errorCode = crCheckFile(initStruct[0], pkBaseStruct[0], checkFileName, n, delFlag == "0" ? 0 : 1, userIdBuf);
						CheckError("cr_check_file", errorCode);
					} finally {
						// закрыть БОК
						IntPtr pCrPkBaseClose = loadMethod(pDll, "cr_pkbase_close");
						CrPkBaseClose crPkBaseClose = (CrPkBaseClose) Marshal.GetDelegateForFunctionPointer(pCrPkBaseClose, typeof(CrPkBaseClose));
						errorCode = crPkBaseClose(pkBaseStruct[0]);
						CheckError("cr_pkbase_close", errorCode);
					}
				} finally {
					// деинициализация
					IntPtr pCrUninit = loadMethod(pDll, "cr_uninit");
					CrUninit crUninit = (CrUninit) Marshal.GetDelegateForFunctionPointer(pCrUninit, typeof(CrUninit));
					errorCode = crUninit(initStruct[0]);
					CheckError("cr_uninit", errorCode);
				}
				Console.WriteLine("Result: SUCCESS");
			} finally {
				// выгрузка библиотеки
				if (pDll != IntPtr.Zero) {
					NativeMethods.FreeLibrary(pDll);
				}
			}
		} catch (Exception e) {
			Console.WriteLine("Result: FAIL");
			Console.WriteLine(e.Message);
		}
		pSw.Stop();
		Console.WriteLine("Execution time: " + pSw.Elapsed.ToString());
	}
	
	/**
		Проверка отсутствия ошибок при вызове библиотечного метода
	*/
	private static void CheckError(string methodName, int errorCode) {
		if (errorCode != 0) {
			throw new Exception("ERROR: method \"" + methodName + 
				"\", code " + errorCode + 
				(ErrorCodes.codes.ContainsKey(errorCode) ? ", text " + ErrorCodes.codes[errorCode] : ""));
		}
	}
	/**
		Поиск метода в библиотеке
	*/
	private static IntPtr loadMethod(IntPtr pDll, string methodName) {
		IntPtr procAddr = NativeMethods.GetProcAddress(pDll, methodName);
		if (procAddr == IntPtr.Zero) {
			throw new Exception ("ERROR: method \"" + methodName + "\" not found in \"" + DLL_NAME + "\"");
		}
		return procAddr;
	}
}