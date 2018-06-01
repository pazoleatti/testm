namespace VSAX3.SnpLibrary
{
    using System;
    using System.Runtime.CompilerServices;

    internal abstract class SnpSchematronObject : ISnpObject
    {
        protected SnpSchematronObject()
        {
        }

        public bool Equals(SnpSchematronObject other)
        {
            return new SnpSchematronObjectSameDimensions().Equals(this, other);
        }

        public virtual void ReplaceValue(string value)
        {
            if (!this.IsConstValue)
            {
                this.Value = value;
            }
        }

        public override string ToString()
        {
            return string.Format("<xsl:variable name=\"{0}\" select=\"{1}\"/>", this.Name, this.Value ?? "'NaN'");
        }

        public virtual bool IsConstValue { get; protected set; }

        public virtual string Name { get; protected set; }

        public virtual string Value { get; protected set; }

        public virtual string XPath { get; protected set; }
    }
}

