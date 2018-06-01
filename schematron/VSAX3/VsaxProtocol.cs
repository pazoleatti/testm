namespace VSAX3
{
    using System;
    using System.IO;
    using System.Text;

    public static class VsaxProtocol
    {
        public static bool WriteProtocolTo(string url, IVsaxErrorHandler errors)
        {
            return WriteProtocolTo(url, Path.GetFileNameWithoutExtension(url), errors);
        }

        public static bool WriteProtocolTo(string url, string xmlName, IVsaxErrorHandler errors)
        {
            using (StreamWriter writer = new StreamWriter(url, false, Encoding.GetEncoding("UTF-8")))
            {
                writer.WriteLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                writer.WriteLine("<?xml-stylesheet type=\"text/xsl\" href=\"Протокол.xsl\"?>");
                writer.WriteLine("<Файл ИмяОбрабФайла=\"" + Path.GetFileNameWithoutExtension(xmlName) + "\" КодОшОб=\"" + ((errors.Errors.Count > 0) ? "0000000001" : "0000000000") + "\">");
                if (errors.Errors.Count > 0)
                {
                    foreach (ErrorsStruct struct2 in errors.Errors)
                    {
                        writer.Write("\t<СвПоОшибке КодСерьезн=\"{0}\" КодОшибки=\"{1}\" ПолОшЭл=\"{2}\" ЗнЭлем=\"{3}\" ИдДокНаим=\"{4}\" ИдДокЗнач=\"{5}\">\r\n", new object[] { struct2.CriticalCode, struct2.ErrorCode, struct2.XPath, struct2.Value, struct2.IdDocNaim, struct2.IdDocZnach });
                        writer.Write("\t\t<ТекстОш><![CDATA[{0}]]></ТекстОш>\r\n", struct2.ErrorText);
                        writer.Write("\t</СвПоОшибке>");
                    }
                }
                writer.Write("\r\n</Файл>");
                writer.Flush();
            }
            return true;
        }
    }
}

