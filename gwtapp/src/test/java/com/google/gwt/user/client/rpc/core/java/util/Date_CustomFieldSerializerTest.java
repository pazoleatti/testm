package com.google.gwt.user.client.rpc.core.java.util;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Custom field serializer for {@link java.util.Date}.
 */
public class Date_CustomFieldSerializerTest{

	private static class TestSerializationStreamReader implements SerializationStreamReader{
		
		long l;
		
		public void setL(long l){
			this.l = l;
		}
		
		@Override
		public String readString() throws SerializationException {
			return null;
		}
		
		@Override
		public short readShort() throws SerializationException {
			return 0;
		}
		
		@Override
		public Object readObject() throws SerializationException {
			return null;
		}
		
		@Override
		public long readLong() throws SerializationException {
			return l;
		}
		
		@Override
		public int readInt() throws SerializationException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public float readFloat() throws SerializationException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double readDouble() throws SerializationException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public char readChar() throws SerializationException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public byte readByte() throws SerializationException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public boolean readBoolean() throws SerializationException {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private static class TestSerializationStreamWriter implements SerializationStreamWriter {
		
		long l;
		
		public long getL(){
			return l;
		}
		
		@Override
		public void writeString(String value) throws SerializationException {
			//
		}
		
		@Override
		public void writeShort(short value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeObject(Object value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeLong(long value) throws SerializationException {
			this.l = value;
		}
		
		@Override
		public void writeInt(int value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeFloat(float value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeDouble(double value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeChar(char value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeByte(byte value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void writeBoolean(boolean value) throws SerializationException {
			// TODO Auto-generated method stub
			
		}
	};

	@Test
	public void test() throws SerializationException {
		Date date = new Date();
		TestSerializationStreamWriter writer = new TestSerializationStreamWriter();
		new Date_CustomFieldSerializer().serializeInstance(writer, date);
		TestSerializationStreamReader reader = new TestSerializationStreamReader();
		reader.setL(writer.getL());
		Date result = new Date_CustomFieldSerializer().instantiateInstance(reader);
		Assert.assertEquals(date, result);
	}

}
