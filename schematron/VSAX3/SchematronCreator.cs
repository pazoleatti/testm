namespace VSAX3
{
    using System;
    using System.IO;
    using System.Reflection;
    using System.Runtime.InteropServices;
    using System.Xml.Schema;
    using System.Xml.Xsl;

    internal class SchematronCreator
    {
        private readonly Stream _schema;

        public SchematronCreator(Stream schema)
        {
            this._schema = schema;
        }

        public SchematronCreator(XmlSchemaSet schema) : this(VsaxXsdService.XmlSchemaSet2Stream(schema))
        {
        }

        public void Close()
        {
            this._schema.Close();
        }

        public MemoryStream Compile()
        {
            string[] strArray = new string[] { "Schematron.xsl1.xsl", "Schematron.xsl2.xsl", "Schematron.xsl3.xsl", "Schematron.xsl4.xsl" };
            MemoryStream pOutputXml = null;
            MemoryStream stream2 = null;
            this.CutAndModifySchematron(this._schema, strArray[0], out pOutputXml);
            this.CutAndModifySchematron(pOutputXml, strArray[1], out stream2);
            pOutputXml.Dispose();
            this.CutAndModifySchematron(stream2, strArray[2], out pOutputXml);
            stream2.Dispose();
            this.CutAndModifySchematron(pOutputXml, strArray[3], out stream2);
            pOutputXml.Dispose();
            return stream2;
        }

        private void CutAndModifySchematron(Stream pInputXml, string resourceName, out MemoryStream pOutputXml)
        {
            string name = base.GetType().Assembly.GetName().Name;
            using (Stream stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(name + "." + resourceName))
            {
                if (stream == null)
                {
                    throw new NullReferenceException("NullReferenceException: inputXsl in SchematronCreator.CutAndModifySchematron");
                }
                pOutputXml = XslTransformation.Transform(pInputXml, stream, new XsltSettings(true, false));
            }
        }
    }
}

