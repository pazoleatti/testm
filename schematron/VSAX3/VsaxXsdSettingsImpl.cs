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

    internal class VsaxXsdSettingsImpl : IVsaxXsdSettings
    {
        private readonly string _schemaName;
        private readonly XmlSchemaSet _schemas;

        public VsaxXsdSettingsImpl(XmlSchemaSet schema, string schemaName = "")
        {
            this._schemas = schema;
            this._schemaName = schemaName;
            this.Paths = null;
            this.XslVariables = null;
            this.ParsingNamespaces();
            if (this.HasSchematronNamespace && this.HasUnisoftNamespace)
            {
                SchematronCreator creator = new SchematronCreator(schema);
                this.Schematron = creator.Compile();
                creator.Close();
            }
            else
            {
                this.Schematron = null;
            }
            this.ParsingVsaxSettings();
        }

        private void ParsingNamespaces()
        {
            foreach (XmlSchema schema in this._schemas.Schemas())
            {
                foreach (XmlQualifiedName name in schema.Namespaces.ToArray())
                {
                    string str = name.ToString();
                    if (str != null)
                    {
                        if (!(str == "http://purl.oclc.org/dsdl/schematron:sch"))
                        {
                            if (str == "http://www.unisoftware.ru/schematron-extensions:usch")
                            {
                                goto Label_007F;
                            }
                            if (str == "http://www.nalogypro.ru/schematron-extensions:snp")
                            {
                                goto Label_0089;
                            }
                        }
                        else
                        {
                            this.HasSchematronNamespace = true;
                        }
                    }
                    continue;
                Label_007F:
                    this.HasUnisoftNamespace = true;
                    continue;
                Label_0089:
                    this.HasSnpNamespace = true;
                }
            }
        }

        private void ParsingSettings(XmlReader r)
        {
            while (r.Read())
            {
                if (!r.IsStartElement())
                {
                    continue;
                }
                string name = r.Name;
                if (name != null)
                {
                    if (!(name == "snp:Фрагменты"))
                    {
                        if (name == "snp:Переменные")
                        {
                            goto Label_005F;
                        }
                        if ((name == "snp:Константы") || (name == "snp:Дополнительно"))
                        {
                        }
                    }
                    else
                    {
                        this.ReadCheckPaths(r.ReadSubtree());
                    }
                }
                continue;
            Label_005F:
                this.ReadSnpVariables(r.ReadSubtree());
            }
            r.Close();
        }

        private void ParsingVsaxSettings()
        {
            foreach (XmlSchema schema in this._schemas.Schemas())
            {
                foreach (XmlSchemaObject obj2 in schema.Items)
                {
                    XmlSchemaAnnotation annotation = obj2 as XmlSchemaAnnotation;
                    if (annotation != null)
                    {
                        foreach (XmlSchemaObject obj3 in annotation.Items)
                        {
                            XmlSchemaAppInfo info = obj3 as XmlSchemaAppInfo;
                            if (info != null)
                            {
                                foreach (XmlNode node in info.Markup)
                                {
                                    XmlElement element = node as XmlElement;
                                    if (element != null)
                                    {
                                        bool flag = element.NamespaceURI == "http://www.nalogypro.ru/schematron-extensions";
                                        bool flag2 = element.GetPrefixOfNamespace("http://www.nalogypro.ru/schematron-extensions").Equals("snp");
                                        if (flag && flag2)
                                        {
                                            this.HasSnpNamespace = true;
                                            this.Paths = new StringDictionary();
                                            this.XslVariables = new SnpSchematronCollection();
                                            StringBuilder builder = new StringBuilder();
                                            builder.AppendLine("<?xml version='1.0'?>");
                                            builder.AppendFormat("<{0} версия='{1}' xmlns:{2}='{3}'>\r\n", new object[] { element.Name, element.GetAttribute("версия"), element.Prefix, element.NamespaceURI });
                                            builder.AppendLine(element.InnerXml);
                                            builder.AppendFormat("</{0}>\r\n", element.Name);
                                            using (XmlReader reader = XmlReader.Create(new StringReader(builder.ToString()), new XmlReaderSettings()))
                                            {
                                                reader.MoveToContent();
                                                if ((reader.IsStartElement() && (reader.LocalName == "vsax")) && (reader.GetAttribute("версия") == "3"))
                                                {
                                                    this.ParsingSettings(reader.ReadSubtree());
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }

        private void ReadCheckPaths(XmlReader r)
        {
            string str = null;
            while (r.Read())
            {
                switch (r.NodeType)
                {
                    case XmlNodeType.Element:
                        str = (r.Name == "snp:Путь") ? r.GetAttribute("тип") : null;
                        break;

                    case XmlNodeType.Text:
                        if (str != null)
                        {
                            string key = r.Value.Trim();
                            if (!this.Paths.ContainsKey(key))
                            {
                                this.Paths.Add(key, str);
                            }
                        }
                        break;
                }
            }
            r.Close();
        }

        private void ReadSnpVariables(XmlReader r)
        {
            while (r.Read())
            {
                if ((r.IsStartElement() && (r.Name == "snp:variable")) && r.HasAttributes)
                {
                    string name = r.MoveToAttribute("name") ? r.Value : null;
                    string xpath = r.MoveToAttribute("select") ? r.Value : null;
                    if ((name != null) && (xpath != null))
                    {
                        string str3 = xpath.Contains("/") ? null : xpath;
                        SnpVariable item = new SnpVariable(name, xpath, str3);
                        if (!this.XslVariables.Contains(item))
                        {
                            this.XslVariables.Add(item);
                        }
                    }
                }
            }
            r.Close();
        }

        public bool HasSchematronNamespace { get; private set; }

        public bool HasSnpNamespace { get; private set; }

        public bool HasUnisoftNamespace { get; private set; }

        public StringDictionary Paths { get; private set; }

        public Stream Schematron { get; private set; }

        public EVsaxValidateType Type { get; private set; }

        public SnpSchematronCollection XslVariables { get; private set; }
    }
}

