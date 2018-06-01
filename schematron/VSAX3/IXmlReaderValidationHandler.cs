namespace VSAX3
{
    using System;
    using System.Collections.Generic;
    using System.Xml.Schema;

    internal interface IXmlReaderValidationHandler
    {
        void XmlReaderValidationEventHandler(object sender, ValidationEventArgs e);

        List<XmlReaderError> XmlErrors { get; }

        bool XmlValidationErrorFound { get; }
    }
}

