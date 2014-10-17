namespace VSAX3
{
    using System;
    using System.Runtime.CompilerServices;
    using System.Xml.Serialization;

    [Serializable, XmlRoot("Errors")]
    public class Errors
    {
        [XmlElement("Error")]
        public Error[] errors { get; set; }

        [Serializable]
        public class Error
        {
            public Error()
            {
            }

            public Error(string xpath, string errcode, string value, string iddocnaim, string iddocznach, string text, string criticalcode)
            {
                this.FullPath = xpath;
                this.CriticalCode = criticalcode;
                this.ErrorCode = errcode;
                this.Value = value;
                this.IdDocNaim = iddocnaim;
                this.IdDocZnach = iddocznach;
                this.Text = text;
            }

            [XmlAttribute("CriticalCode")]
            public string CriticalCode { get; set; }

            [XmlAttribute("ErrorCode")]
            public string ErrorCode { get; set; }

            [XmlAttribute("FullPath")]
            public string FullPath { get; set; }

            [XmlAttribute("IdDocNaim")]
            public string IdDocNaim { get; set; }

            [XmlAttribute("IdDocZnach")]
            public string IdDocZnach { get; set; }

            [XmlText]
            public string Text { get; set; }

            [XmlAttribute("Value")]
            public string Value { get; set; }
        }
    }
}

