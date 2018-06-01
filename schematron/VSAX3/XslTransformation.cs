namespace VSAX3
{
    using System;
    using System.IO;
    using System.Xml;
    using System.Xml.Xsl;

    internal static class XslTransformation
    {
        public static MemoryStream Transform(Stream inputxml, Stream inputxsl, XsltSettings settings)
        {
            MemoryStream stream = new MemoryStream {
                Position = 0L
            };
            inputxml.Position = 0L;
            inputxsl.Position = 0L;
            using (XmlReader reader = XmlReader.Create(inputxml))
            {
                using (XmlReader reader2 = XmlReader.Create(inputxsl))
                {
                    XslCompiledTransform transform = new XslCompiledTransform();
                    transform.Load(reader2, settings, new XmlUrlResolver());
                    transform.Transform(reader, null, (Stream) stream);
                }
            }
            stream.Position = 0L;
            return stream;
        }
    }
}

