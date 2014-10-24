using System;
using System.Diagnostics;
using System.IO;
using System.Xml.Schema;
using VSAX3; //Версия 3.3.147.101

class Schematron {
	static void Main(string[] args) {
		Stopwatch pSw = new Stopwatch();
		pSw.Start();
		try {
			// Проверка количества аргументов. Вывод справки
			if (args.Length != 2) {
				throw new Exception (
					"Program description: Check xml by xsd scheme with schematron extension.\n" +
					"Usage syntax: Schematron.exe \"PATH_TO_XML_FILE\" \"PATH_TO_XSD_FILE\"\n" +
					"Result: Warnings or info of success result will be writed in system out.");
			}
			string xmlFleName = args[0];
			string xsdFleName = args[1];
			
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
			reader.ErrorCounter = 100;
			// #3: Валидация Xml по Xsd со схематроном 'snp' (Способ проверки добавленный в VSAX3* )
			// Версия: (3.3.145.301)+
			// * - расширение схематрона для пофрагментарной обработки файла (фирменный метод решающий вопрос проверки больших файлов, но 
			// затратный по времени валидации по сравнению с предыдущим способом).
			//var reader = new SnpXmlValidatingReader { ValidatingType = EVsaxValidateType.SaxSchematronSnp };
			
			bool result = reader.Validate(pXml, Path.GetFileNameWithoutExtension(xmlFleName), pXsd);
			IVsaxErrorHandler errors = reader.ErrorHandler;
			Console.WriteLine(result && errors.Errors.Count == 0 ? "Result: SUCCESS" : "Result: FAIL");
			foreach (ErrorsStruct e in errors.Errors) {
				Console.WriteLine(e.XPath + " : " + e.ErrorText);
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