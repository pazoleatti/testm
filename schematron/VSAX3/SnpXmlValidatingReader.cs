namespace VSAX3
{
    using System;
    using System.Collections.Specialized;
    using System.Globalization;
    using System.IO;
    using System.Runtime.CompilerServices;
    using System.Text;
    using System.Threading;
    using System.Xml;
    using System.Xml.Schema;
    using System.Xml.Xsl;
    using VSAX3.Schematron;
    using VSAX3.SnpLibrary;

    public class SnpXmlValidatingReader
    {
        private readonly IVsaxErrors _errorHandler = new VsaxErrorHandlerImpl();

        public SnpXmlValidatingReader()
        {
            Thread.CurrentThread.CurrentCulture = CultureInfo.CreateSpecificCulture("ru-RU");
            Thread.CurrentThread.CurrentUICulture = CultureInfo.CreateSpecificCulture("ru-RU");
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
            XmlReaderSettings settings = new XmlReaderSettings {
                ValidationType = ValidationType.Schema
            };
            settings.Schemas.Add(pXmlSchemaSet);
            using (reader = XmlReader.Create(pXmlStream, settings))
            {
                flag = PrepareValidatingXml(reader);
            }
            if (!flag || (this.ValidatingType != EVsaxValidateType.SnpSax))
            {
                MemoryStream stream;
                XslCompiledTransform transform;
                XsltArgumentList list;
                UnisoftFunctions functions;
                Errors errors;
                pXmlStream.Position = 0L;
                IVsaxXsdSettings settings2 = new VsaxXsdSettingsImpl(pXmlSchemaSet, sXsdName);
                switch (this.ValidatingType)
                {
                    case EVsaxValidateType.SnpSax:
                        using (SaxXmlReader reader2 = new SaxXmlReader(pXmlStream, settings, this._errorHandler))
                        {
                            while (reader2.Read())
                            {
                            }
                        }
                        break;

                    case EVsaxValidateType.SaxSchematronUsch:
                        settings.CloseInput = false;
                        if (flag)
                        {
                            pXmlStream.Position = 0L;
                            using (stream = new MemoryStream())
                            {
                                settings2.Schematron.Position = 0L;
                                using (reader = XmlReader.Create(settings2.Schematron, new XmlReaderSettings()))
                                {
                                    transform = new XslCompiledTransform();
                                    XmlUrlResolver stylesheetResolver = new XmlUrlResolver();
                                    XsltSettings settings3 = new XsltSettings(false, false);
                                    list = new XsltArgumentList();
                                    functions = new UnisoftFunctions(sXmlName);
                                    list.AddExtensionObject("http://www.unisoftware.ru/schematron-extensions", functions);
                                    int errcounter = 0;
                                    list.XsltMessageEncountered += delegate (object param0, XsltMessageEncounteredEventArgs param1) {
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
                                            transform.Transform(XmlReader.Create(pXmlStream), list, writer);
                                        }
                                        catch (Exception)
                                        {
                                        }
                                        writer.WriteEndElement();
                                    }
                                }
                                stream.Position = 0L;
                                errors = SerializeSchematronErrors.ParseXmlErrors<Errors>(stream);
                                if ((errors != null) && (errors.errors != null))
                                {
                                    foreach (Errors.Error error in errors.errors)
                                    {
                                        this._errorHandler.PushValidatingError(new ErrorsStruct(error.FullPath, error.Text, error.Value, error.ErrorCode, error.IdDocNaim, error.IdDocZnach, error.CriticalCode));
                                    }
                                }
                            }
                            break;
                        }
                        using (SaxXmlReader reader3 = new SaxXmlReader(pXmlStream, settings, this._errorHandler))
                        {
                            while (reader3.Read())
                            {
                            }
                        }
                        break;

                    case EVsaxValidateType.SaxSchematronSnp:
                    {
                        StringDictionary paths = settings2.Paths;
                        using (DomXmlReader reader4 = new DomXmlReader(pXmlStream, settings, paths, this._errorHandler))
                        {
                            reader4.Variables = settings2.XslVariables;
                            while (reader4.Read())
                            {
                                using (stream = new MemoryStream())
                                {
                                    settings2.Schematron.Position = 0L;
                                    StreamReader reader5 = new StreamReader(settings2.Schematron);
                                    StringBuilder builder = new StringBuilder();
                                    foreach (SnpSchematronObject obj2 in settings2.XslVariables)
                                    {
                                        builder.AppendLine("\t" + obj2.ToString());
                                    }
                                    string s = reader5.ReadToEnd().Replace("<!--$SnpVariables$-->", builder.ToString());
                                    functions = new UnisoftFunctions(sXmlName);
                                    list = new XsltArgumentList();
                                    list.AddExtensionObject("http://www.unisoftware.ru/schematron-extensions", functions);
                                    XsltSettings settings6 = new XsltSettings(true, true);
                                    using (reader = XmlReader.Create(new StringReader(s), new XmlReaderSettings()))
                                    {
                                        transform = new XslCompiledTransform();
                                        transform.Load(reader, settings6, new XmlUrlResolver());
                                        transform.Transform(XmlReader.Create(new StringReader("<?xml version='1.0' encoding='utf-8'?>\r\n" + reader4.XmlDomValue)), list, stream);
                                        stream.Position = 0L;
                                    }
                                    errors = SerializeSchematronErrors.ParseXmlErrors<Errors>(stream);
                                    if ((errors == null) || (errors.errors == null))
                                    {
                                        continue;
                                    }
                                    foreach (Errors.Error error in errors.errors)
                                    {
                                        ErrorsStruct err = new ErrorsStruct(SnpVsaxPathReplace.PathConcat(reader4.XmlDomPath, reader4.XmlDomXPath, error.FullPath), error.Text, error.Value, error.ErrorCode, error.IdDocNaim, error.IdDocZnach, "");
                                        this._errorHandler.PushValidatingError(err);
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
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

