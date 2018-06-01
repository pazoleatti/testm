namespace VSAX3
{
    using System;
    using System.Runtime.CompilerServices;
    using System.Xml.Schema;

    internal class XmlReaderError
    {
        public XmlReaderError(XmlSeverityType type, string message)
        {
            this.Message = message;
            this.Type = type;
        }

        public string Message { get; private set; }

        public XmlSeverityType Type { get; private set; }
    }
}

