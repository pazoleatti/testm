namespace VSAX3
{
    using System;
    using System.IO;
    using System.Runtime.CompilerServices;
    using System.Text;
    using System.Xml;
    using System.Xml.Schema;
    using System.Xml.Xsl;
    using VSAX3.Schematron;

    public class SnpXmlValidatingReader
    {
        private readonly IVsaxErrors _errorHandler = new VsaxErrorHandlerImpl();

        public SnpXmlValidatingReader()
        {
            this.ValidatingType = EVsaxValidateType.SnpSax;
            this.ErrorCounter = 100;
        }

        public void Close()
        {
            this._errorHandler.Clear();
        }

        private static bool PrepareValidatingXml(XmlReader reader)
        {
            bool flag = true;
            try
            {
                while (reader.Read())
                {
                }
            }
            catch (XmlException)
            {
                flag = false;
            }
            catch (XmlSchemaException)
            {
                flag = false;
            }
            return flag;
        }

        public bool Validate(Stream pXmlStream, string sXmlName, XmlSchemaSet pXmlSchemaSet)
        {
            return this.Validate(pXmlStream, sXmlName, pXmlSchemaSet, string.Empty);
        }

        public bool Validate(Stream pXmlStream, string sXmlName, XmlSchemaSet pXmlSchemaSet, string sXsdName)
        {
            return this.Validating(pXmlStream, sXmlName, pXmlSchemaSet, sXsdName);
        }

        private bool Validating(Stream pXmlStream, string sXmlName, XmlSchemaSet pXmlSchemaSet, string sXsdName)
        {
            bool flag;
            XmlReader reader;
            SaxXmlReader reader2;
            XmlReaderSettings settings = new XmlReaderSettings {
                ValidationType = ValidationType.Schema,
                CloseInput = false
            };
            settings.Schemas.Add(pXmlSchemaSet);
            using (reader = XmlReader.Create(pXmlStream, settings))
            {
                flag = PrepareValidatingXml(reader);
            }
            pXmlStream.Position = 0L;
            switch (this.ValidatingType)
            {
                case EVsaxValidateType.SnpSax:
                    if (!flag)
                    {
                        using (reader2 = new SaxXmlReader(pXmlStream, settings, this._errorHandler, this.ErrorCounter))
                        {
                            while (reader2.Read())
                            {
                            }
                        }
                        break;
                    }
                    return true;

                case EVsaxValidateType.SaxSchematronUsch:
                    if (flag)
                    {
                        IVsaxXsdSettings settings2 = new VsaxXsdSettingsImpl(pXmlSchemaSet, sXsdName);
                        if (settings2.HasSchematronNamespace && settings2.HasUnisoftNamespace)
                        {
                            using (MemoryStream stream = new MemoryStream())
                            {
                                settings2.Schematron.Position = 0L;
                                using (reader = XmlReader.Create(settings2.Schematron, new XmlReaderSettings()))
                                {
                                    XslCompiledTransform transform = new XslCompiledTransform();
                                    XmlUrlResolver stylesheetResolver = new XmlUrlResolver();
                                    XsltSettings settings3 = new XsltSettings(false, false);
                                    XsltArgumentList arguments = new XsltArgumentList();
                                    UnisoftFunctions extension = new UnisoftFunctions(sXmlName);
                                    arguments.AddExtensionObject("http://www.unisoftware.ru/schematron-extensions", extension);
                                    int errcounter = 0;
                                    arguments.XsltMessageEncountered += delegate (object param0, XsltMessageEncounteredEventArgs param1) {
                                        if (errcounter == this.ErrorCounter)
                                        {
                                            throw new Exception();
                                        }
                                        errcounter++;
                                    };
                                    transform.Load(reader, settings3, stylesheetResolver);
                                    XmlWriterSettings settings4 = new XmlWriterSettings {
                                        Encoding = Encoding.UTF8,
                                        Indent = true,
                                        ConformanceLevel = ConformanceLevel.Document
                                    };
                                    using (XmlWriter writer = XmlWriter.Create(stream, settings4))
                                    {
                                        writer.WriteStartElement("Errors");
                                        try
                                        {
                                            transform.Transform(XmlReader.Create(pXmlStream), arguments, writer);
                                        }
                                        catch (Exception)
                                        {
                                        }
                                        writer.WriteEndElement();
                                    }
                                }
                                stream.Position = 0L;
                                Errors errors = SerializeSchematronErrors.ParseXmlErrors<Errors>(stream);
                                if ((errors != null) && (errors.errors != null))
                                {
                                    foreach (Errors.Error error in errors.errors)
                                    {
                                        this._errorHandler.PushValidatingError(new ErrorsStruct(error.FullPath, error.Text, error.Value, error.ErrorCode, error.IdDocNaim, error.IdDocZnach, error.CriticalCode));
                                    }
                                }
                            }
                        }
                        break;
                    }
                    using (reader2 = new SaxXmlReader(pXmlStream, settings, this._errorHandler, this.ErrorCounter))
                    {
                        while (reader2.Read())
                        {
                        }
                    }
                    break;
            }
            return true;
        }

        public ushort ErrorCounter { get; set; }

        public IVsaxErrorHandler ErrorHandler
        {
            get
            {
                return this._errorHandler;
            }
        }

        public EVsaxValidateType ValidatingType { get; set; }
    }
}

