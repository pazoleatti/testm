namespace VSAX3
{
    using System;
    using System.Collections;
    using System.Collections.Generic;
    using System.IO;
    using System.Linq;
    using System.Runtime.CompilerServices;
    using System.Xml;
    using System.Xml.Schema;

    internal class XPathXmlReader : XmlReader, IXmlReaderValidationHandler
    {
        private int _lastCount;
        private string _lastPath;
        private readonly SnpStack<string> _pPath = new SnpStack<string>();
        private int _presCount;
        private readonly Hashtable _pTagsTbl = new Hashtable();
        private readonly XmlReader _reader;

        protected XPathXmlReader(Stream pXmlStream, XmlReaderSettings settings)
        {
            this.XmlErrors = new List<XmlReaderError>();
            if (settings.ValidationType == ValidationType.Schema)
            {
                settings.ValidationEventHandler += new ValidationEventHandler(this.XmlReaderValidationEventHandler);
            }
            this._reader = XmlReader.Create(pXmlStream, settings);
        }

        public override void Close()
        {
            this._pTagsTbl.Clear();
            this._pPath.Clear();
            this.XmlErrors.Clear();
            this._reader.Close();
        }

        public override string GetAttribute(int i)
        {
            return this._reader.GetAttribute(i);
        }

        public override string GetAttribute(string name)
        {
            return this._reader.GetAttribute(name);
        }

        public override string GetAttribute(string name, string namespaceUri)
        {
            return this._reader.GetAttribute(name, namespaceUri);
        }

        public override string LookupNamespace(string prefix)
        {
            return this._reader.LookupNamespace(prefix);
        }

        public override bool MoveToAttribute(string name)
        {
            return this._reader.MoveToAttribute(name);
        }

        public override bool MoveToAttribute(string name, string ns)
        {
            return this._reader.MoveToAttribute(name, ns);
        }

        public override bool MoveToElement()
        {
            return this._reader.MoveToElement();
        }

        public override bool MoveToFirstAttribute()
        {
            return this._reader.MoveToFirstAttribute();
        }

        public override bool MoveToNextAttribute()
        {
            return this._reader.MoveToNextAttribute();
        }

        public override bool Read()
        {
            return this.ReadPath();
        }

        public override bool ReadAttributeValue()
        {
            return this._reader.ReadAttributeValue();
        }

        private bool ReadPath()
        {
            bool isEmptyElement;
            Func<string, bool> predicate = null;
            if (!this._reader.Read())
            {
                return !this._reader.EOF;
            }
            switch (this._reader.NodeType)
            {
                case XmlNodeType.Element:
                    this.Path = string.Empty;
                    this.XPath = string.Empty;
                    isEmptyElement = this._reader.IsEmptyElement;
                    this._pPath.Push(this._reader.Name);
                    this.Path = this._pPath.ToString();
                    this._lastCount = this._presCount;
                    this._presCount = this._pPath.Count;
                    if (!this._pTagsTbl.Contains(this.Path))
                    {
                        this._pTagsTbl.Add(this.Path, 1);
                        break;
                    }
                    this._pTagsTbl[this.Path] = ((int) this._pTagsTbl[this.Path]) + 1;
                    break;

                case XmlNodeType.EndElement:
                    this._pPath.Pop();
                    this._lastCount = this._presCount;
                    this._presCount = this._pPath.Count;
                    if ((this._lastCount - this._presCount) > 0)
                    {
                        if (predicate == null)
                        {
                            predicate = v => (v.Split(new char[] { '/' }).Count<string>() - 1) > (this._presCount + 1);
                        }
                        string[] strArray = this._pTagsTbl.Keys.Cast<string>().Where<string>(predicate).ToArray<string>();
                        foreach (string str2 in strArray)
                        {
                            this._pTagsTbl.Remove(str2);
                        }
                    }
                    goto Label_0303;

                default:
                    goto Label_0303;
            }
            this._lastPath = string.Empty;
            foreach (string str in this._pPath.Reverse<string>())
            {
                this._lastPath = this._lastPath + "/" + str;
                if (this._pTagsTbl.ContainsKey(this._lastPath))
                {
                    object xPath = this.XPath;
                    this.XPath = string.Concat(new object[] { xPath, "/", str, "[", (int) this._pTagsTbl[this._lastPath], "]" });
                }
                else
                {
                    this.XPath = this.XPath + "/" + str + "[?]";
                }
            }
            this._lastPath = this._pPath.ToString();
            if (isEmptyElement)
            {
                this._pPath.Pop();
            }
        Label_0303:
            return !this._reader.EOF;
        }

        public override void ResolveEntity()
        {
            this._reader.ResolveEntity();
        }

        public virtual void XmlReaderValidationEventHandler(object sender, ValidationEventArgs e)
        {
            this.XmlValidationErrorFound = true;
            this.XmlErrors.Add(new XmlReaderError(e.Severity, e.Message));
        }

        public override int AttributeCount
        {
            get
            {
                return this._reader.AttributeCount;
            }
        }

        public override string BaseURI
        {
            get
            {
                return this._reader.BaseURI;
            }
        }

        public override int Depth
        {
            get
            {
                return this._reader.Depth;
            }
        }

        public override bool EOF
        {
            get
            {
                return this._reader.EOF;
            }
        }

        public override bool IsEmptyElement
        {
            get
            {
                return this._reader.IsEmptyElement;
            }
        }

        public override string LocalName
        {
            get
            {
                return this._reader.LocalName;
            }
        }

        public override string NamespaceURI
        {
            get
            {
                return this._reader.NamespaceURI;
            }
        }

        public override XmlNameTable NameTable
        {
            get
            {
                return this._reader.NameTable;
            }
        }

        public override XmlNodeType NodeType
        {
            get
            {
                return this._reader.NodeType;
            }
        }

        protected string Path { get; private set; }

        public override string Prefix
        {
            get
            {
                return this._reader.Prefix;
            }
        }

        public override System.Xml.ReadState ReadState
        {
            get
            {
                return this._reader.ReadState;
            }
        }

        public override string Value
        {
            get
            {
                return this._reader.Value;
            }
        }

        public List<XmlReaderError> XmlErrors { get; private set; }

        public bool XmlValidationErrorFound { get; protected set; }

        protected string XPath { get; private set; }
    }
}

