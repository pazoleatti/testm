namespace VSAX3
{
    using System;
    using System.Collections.Specialized;
    using System.IO;
    using System.Runtime.CompilerServices;
    using System.Runtime.InteropServices;
    using System.Text;
    using System.Xml;
    using System.Xml.Schema;
    using VSAX3.SnpLibrary;

    internal class DomXmlReader : XPathXmlReader
    {
        private readonly IVsaxErrorPusher _errorCatching;
        private readonly StringDictionary _paths;
        private readonly StringBuilder _xmlDom;

        public DomXmlReader(Stream pXmlStream, XmlReaderSettings settings, StringDictionary paths, IVsaxErrorPusher errorCatching = null) : base(pXmlStream, settings)
        {
            this._xmlDom = new StringBuilder();
            this._paths = paths;
            this._errorCatching = errorCatching;
        }

        public override void Close()
        {
            this._xmlDom.Clear();
            base.Close();
        }

        private void DescendantOrSelf(string endingElement)
        {
            bool flag = true;
            while (!this.EOF && flag)
            {
                if (this.NodeType == XmlNodeType.Element)
                {
                    if ((this.Variables != null) && (this.Variables.Count != 0))
                    {
                        this.UpdateVariable(this.Variables);
                    }
                    if ((this.Consts != null) && (this.Consts.Count != 0))
                    {
                        this.UpdateVariable(this.Consts);
                    }
                    if (base.XmlValidationErrorFound)
                    {
                        base.XmlValidationErrorFound = false;
                        foreach (XmlReaderError error in base.XmlErrors)
                        {
                            this._errorCatching.PushValidatingError(new ErrorsStruct(base.XPath, error.Message, "", "0300300001", "", "", ""));
                        }
                        base.XmlErrors.Clear();
                    }
                }
                switch (this.NodeType)
                {
                    case XmlNodeType.Element:
                    {
                        bool isEmptyElement = this.IsEmptyElement;
                        this._xmlDom.Append(string.Format("<{0}", this.Name));
                        if (this.HasAttributes)
                        {
                            this.MoveToFirstAttribute();
                            do
                            {
                                this._xmlDom.Append(string.Format(" {0}=\"{1}\"", this.Name, this.Value));
                            }
                            while (this.MoveToNextAttribute());
                        }
                        this._xmlDom.AppendLine(isEmptyElement ? "/>" : ">");
                        if (endingElement == null)
                        {
                            flag = false;
                        }
                        break;
                    }
                    case XmlNodeType.Text:
                        this._xmlDom.AppendLine(this.Value);
                        break;

                    case XmlNodeType.EndElement:
                        this._xmlDom.AppendLine(string.Format("</{0}>", this.Name));
                        if ((endingElement != null) && (this.Name == endingElement))
                        {
                            flag = false;
                        }
                        break;
                }
                if (flag)
                {
                    base.Read();
                }
            }
        }

        private bool DomRead()
        {
            bool flag = false;
            if (this.NodeType == XmlNodeType.Element)
            {
                if ((this.Variables != null) && (this.Variables.Count != 0))
                {
                    this.UpdateVariable(this.Variables);
                }
                if ((this.Consts != null) && (this.Consts.Count != 0))
                {
                    this.UpdateVariable(this.Consts);
                }
                string endingElement = this.IsEmptyElement ? null : this.Name;
                if (!this._paths.ContainsKey(base.Path))
                {
                    return flag;
                }
                flag = true;
                this.XmlDomPath = base.Path;
                this.XmlDomXPath = base.XPath;
                string str2 = this._paths[base.Path];
                if (str2 == null)
                {
                    return flag;
                }
                if (!(str2 == "descendant-or-self::*"))
                {
                    if (str2 != "self::*")
                    {
                        return flag;
                    }
                }
                else
                {
                    this.DescendantOrSelf(endingElement);
                    return flag;
                }
                this.Self();
            }
            return flag;
        }

        public override bool Read()
        {
            this._xmlDom.Clear();
            while (base.Read())
            {
                if ((this.NodeType == XmlNodeType.Element) && base.XmlValidationErrorFound)
                {
                    base.XmlValidationErrorFound = false;
                    foreach (XmlReaderError error in base.XmlErrors)
                    {
                        this._errorCatching.PushValidatingError(new ErrorsStruct(base.XPath, error.Message, "", "0300300001", "", "", ""));
                    }
                    base.XmlErrors.Clear();
                }
                if (this.DomRead())
                {
                    break;
                }
            }
            return !this.EOF;
        }

        private void Self()
        {
            if (this.NodeType == XmlNodeType.Element)
            {
                this._xmlDom.Append(string.Format("<{0}", this.Name));
                if (this.HasAttributes)
                {
                    this.MoveToFirstAttribute();
                    do
                    {
                        this._xmlDom.Append(string.Format(" {0}=\"{1}\"", this.Name, this.Value));
                    }
                    while (this.MoveToNextAttribute());
                }
                this._xmlDom.AppendLine("/>");
            }
        }

        private void UpdateVariable(SnpSchematronCollection col)
        {
            if (this.IsStartElement() && this.HasAttributes)
            {
                this.MoveToFirstAttribute();
                do
                {
                    int num = col.ContainXPath(base.Path + "/@" + this.Name);
                    if (num != -1)
                    {
                        col[num].ReplaceValue(this.Value);
                    }
                }
                while (this.MoveToNextAttribute());
                this.MoveToElement();
            }
        }

        public override void XmlReaderValidationEventHandler(object sender, ValidationEventArgs e)
        {
            if (this._errorCatching != null)
            {
                if (!(base.XmlValidationErrorFound || (base.XmlErrors.Count == 0)))
                {
                    base.XmlErrors.Clear();
                }
                base.XmlValidationErrorFound = true;
                base.XmlErrors.Add(new XmlReaderError(e.Severity, e.Message));
            }
        }

        public SnpSchematronCollection Consts { get; set; }

        public SnpSchematronCollection Variables { get; set; }

        public string XmlDomPath { get; private set; }

        public string XmlDomValue
        {
            get
            {
                return this._xmlDom.ToString();
            }
        }

        public string XmlDomXPath { get; private set; }
    }
}

