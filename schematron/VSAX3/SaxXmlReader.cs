namespace VSAX3
{
    using System;
    using System.IO;
    using System.Xml;
    using System.Xml.Schema;

    internal class SaxXmlReader : XPathXmlReader
    {
        private long _errorCounter;
        private readonly IVsaxErrorPusher _errorHandler;
        private readonly ushort _errorLimit;

        public SaxXmlReader(Stream pXmlStream, XmlReaderSettings settings, IVsaxErrorPusher errorHandler, ushort errorLimit) : base(pXmlStream, settings)
        {
            this._errorHandler = errorHandler;
            this._errorLimit = errorLimit;
        }

        public override bool Read()
        {
            bool flag = base.Read();
            if ((this.NodeType == XmlNodeType.Element) && base.XmlValidationErrorFound)
            {
                base.XmlValidationErrorFound = false;
                foreach (XmlReaderError error in base.XmlErrors)
                {
                    this._errorHandler.PushValidatingError(new ErrorsStruct(base.XPath, error.Message, "", "0300300001", "", "", ""));
                }
                this._errorCounter += base.XmlErrors.Count;
                base.XmlErrors.Clear();
            }
            return ((this._errorCounter < this._errorLimit) && flag);
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

