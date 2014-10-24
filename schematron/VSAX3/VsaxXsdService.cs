namespace VSAX3
{
    using System;
    using System.IO;
    using System.Xml.Schema;

    internal static class VsaxXsdService
    {
        public static Stream XmlSchemaSet2Stream(XmlSchemaSet pXmlSchemaSet)
        {
            MemoryStream stream = new MemoryStream();
            foreach (XmlSchema schema in pXmlSchemaSet.Schemas())
            {
                schema.Write(stream);
            }
            stream.Position = 0L;
            return stream;
        }
    }
}

