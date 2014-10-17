namespace VSAX3
{
    using System;
    using System.IO;
    using System.Xml;
    using System.Xml.Serialization;

    internal static class SerializeSchematronErrors
    {
        public static T ParseXmlErrors<T>(Stream pXmlStream) where T: class
        {
            XmlReaderSettings settings = new XmlReaderSettings {
                ConformanceLevel = ConformanceLevel.Document
            };
            using (XmlReader reader = XmlReader.Create(pXmlStream, settings))
            {
                return (new XmlSerializer(typeof(T)).Deserialize(reader) as T);
            }
        }
    }
}

