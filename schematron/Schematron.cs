using Snp.VSAX3.BF.Xml.Schema.Schematron.Extensions;
using Snp.VSAX3.Contract.Xml.Schema;
using System;
using System.Diagnostics;
using System.Globalization;
using System.Threading;
using System.IO;
using System.Xml;
using System.Xml.Schema;

class Program
{
    static void Main(string[] args)
    {
        Thread.CurrentThread.CurrentUICulture = CultureInfo.GetCultureInfo("ru-RU");
        Stopwatch pSw = new Stopwatch();
        pSw.Start();
        try
        {
            // Проверка количества аргументов. Вывод справки
            if (args.Length != 2 && args.Length != 3)
            {
                throw new Exception(
                    "Program description: Check xml by xsd scheme with schematron extension.\n" +
                    "Usage syntax1: Schematron.exe \"PATH_TO_XML_FILE\" \"PATH_TO_XSD_FILE\" \n" +
                    "Usage syntax2: Schematron.exe \"PATH_TO_XML_FILE\" \"PATH_TO_XSD_FILE\" \"TARGET_XML_FILE_NAME\"\n" +
                    "Result: Warnings or info of success result will be writed in system out.");
            }
            string xmlFilePath = args[0];
            string xsdFilePath = args[1];
            string xmlFileName = args.Length == 2 ? xmlFilePath : args[2];

            var xmlStream = File.OpenRead(xmlFilePath);
            var schema = new XmlSchemaSet();
            schema.Add("", xsdFilePath);

            var readerSettings = new XmlReaderSettings()
            {
                ValidationType = ValidationType.Schema,
                Schemas = schema
            };
            var validatorSettings = new SnpXmlValidatorSettings
            {
                ValidatingType = EValidateType.FormatAndLogic,
                ErrorsLimit = 10000,
                Settings = readerSettings
            };
            var validator = new SnpXmlValidator(validatorSettings);

            string fileNameWithoutExtension = Path.GetFileNameWithoutExtension(xmlFileName);
            var result = validator.Validate(new Tuple<Stream, string>(xmlStream, fileNameWithoutExtension));

            Console.WriteLine(result.IsSuccess && result.Errors.Count == 0 ? "Result: SUCCESS" : "Result: FAIL");
            int errcounter = 0;
            foreach (var e in result.Errors)
            {
                if (errcounter > 10000)
                {
                    break;
                }
                Console.WriteLine(e.XPath + " : " + e.Message);
                errcounter++;
            }
        }
        catch (Exception e) {
            Console.WriteLine("Result: FAIL");
            Console.WriteLine(e.Message);
        }
        pSw.Stop();
        Console.WriteLine("Execution time: " + pSw.Elapsed.ToString());
    }
}
