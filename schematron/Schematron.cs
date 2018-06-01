using System;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Threading;
using System.Xml.Schema;
using VSAX3; //Версия 3.3.147.101

class Schematron {

	// параметры командной строки
	private static string[] args;

	static void Main(string[] appArgs) {
		args = appArgs;
    	Thread t = new Thread(check);
		t.CurrentCulture = new CultureInfo("ru-RU");
		t.Start();
		t.Join();
	}

	static void check() {
		Stopwatch pSw = new Stopwatch();
		pSw.Start();
		try {
			// Проверка количества аргументов. Вывод справки
			if (args.Length != 2 && args.Length != 3) {
				throw new Exception (
					"Program description: Check xml by xsd scheme with schematron extension.\n" +
					"Usage syntax1: Schematron.exe \"PATH_TO_XML_FILE\" \"PATH_TO_XSD_FILE\" \n" +
					"Usage syntax2: Schematron.exe \"PATH_TO_XML_FILE\" \"PATH_TO_XSD_FILE\" \"TARGET_XML_FILE_NAME\"\n" +
					"Result: Warnings or info of success result will be writed in system out.");
			}
			string xmlFleName = args[0];
			string xsdFleName = args[1];
			string xmlTargetFileName = args.Length == 2 ? xmlFleName : args[2];

			FileStream pXml = new FileStream(xmlFleName, FileMode.Open, FileAccess.Read);
			XmlSchemaSet pXsd = new System.Xml.Schema.XmlSchemaSet();
			pXsd.Add("", xsdFleName);

			// #1: Валидация Xml по Xsd без схематрона
			// Версия: (3.3.145.301)+
			//var reader = new SnpXmlValidatingReader {ValidatingType = EVsaxValidateType.SnpSax};

			// #2: Валидация Xml по Xsd со схематроном 'usch' с ограничением кол-ва ошибок по схематрону 100:
			// Версия: (3.3.147.101)+
			SnpXmlValidatingReader reader = new SnpXmlValidatingReader ();// { ValidatingType = EVsaxValidateType.SaxSchematronUsch, ErrorCounter = 100 };
			reader.ValidatingType = EVsaxValidateType.SaxSchematronUsch;
			reader.ErrorCounter = 10000;
			// #3: Валидация Xml по Xsd со схематроном 'snp' (Способ проверки добавленный в VSAX3* )
			// Версия: (3.3.145.301)+
			// * - расширение схематрона для пофрагментарной обработки файла (фирменный метод решающий вопрос проверки больших файлов, но
			// затратный по времени валидации по сравнению с предыдущим способом).
			//var reader = new SnpXmlValidatingReader { ValidatingType = EVsaxValidateType.SaxSchematronSnp };

			bool result = reader.Validate(pXml, xmlTargetFileName, pXsd);
			IVsaxErrorHandler errors = reader.ErrorHandler;
			Console.WriteLine(result && errors.Errors.Count == 0 ? "Result: SUCCESS" : "Result: FAIL");
			int errcounter = 0;
			foreach (ErrorsStruct e in errors.Errors) {
				if (errcounter > 10000)
				{
					break;
				}
				Console.WriteLine(e.XPath + " : " + e.ErrorText);
				errcounter++;
			}
			//VsaxProtocol.WriteProtocolTo("Protocol.xml", errors);

			reader.Close();
		} catch (Exception e) {
			Console.WriteLine("Result: FAIL");
			Console.WriteLine(e.Message);
		}
		pSw.Stop();
		Console.WriteLine("Execution time: " + pSw.Elapsed.ToString());
	}
}