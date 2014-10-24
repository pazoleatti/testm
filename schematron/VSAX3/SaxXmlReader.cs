namespace VSAX3
{
    using System;
    using System.IO;
    using System.Xml;
    using System.Xml.Schema;

    internal class SaxXmlReader : XPathXmlReader
    {
        private readonly IVsaxErrorPusher _errorHandler;

        public SaxXmlReader(Stream pXmlStream, XmlReaderSettings settings, IVsaxErrorPusher errorHandler) : base(pXmlStream, settings)
        {
            this._errorHandler = errorHandler;
        }

        public override bool Read()
        {
            bool flag = base.Read();
            if (this.NodeType == XmlNodeType.Element)
            {
                if (!base.XmlValidationErrorFound)
                {
                    return flag;
                }
                base.XmlValidationErrorFound = false;
                foreach (XmlReaderError error in base.XmlErrors)
                {
                    this._errorHandler.PushValidatingError(new ErrorsStruct(base.XPath, error.Message, "", "0300300001", "", "", ""));
                }
                base.XmlErrors.Clear();
            }
            return flag;
        }

        public override void XmlReaderValidationEventHandler(object sender, ValidationEventArgs e)
        {
            if (!(base.XmlValidationErrorFound || (base.XmlErrors.Count == 0)))
            {
                base.XmlErrors.Clear();
            }
            base.XmlValidationErrorFound = true;
            base.XmlErrors.Add(new XmlReaderError(e.Severity, e.Message));
        }
    }
}

