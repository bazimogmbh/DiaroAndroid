package com.pixelcrater.Diaro.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * IO Utilities. Based on code from commons-io
 */
public class DeprecatedIOUtil 
{
	public static byte[] toByteArray(InputStream in) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read = 0;
		byte[] buffer = new byte[1024];
		while (read != -1) {
			read = in.read(buffer);
			if (read != -1)
				out.write(buffer, 0, read);
		}
		out.close();
		return out.toByteArray();
	}

	public static String toString(InputStream in) throws IOException 
	{
		StringWriter out = new StringWriter();
		copy(new InputStreamReader(in), out);
		out.close();
		in.close();
		return out.toString();
	}

	public static int copy(InputStream in, OutputStream out) throws IOException 
	{
		int count = 0;
		int read = 0;
		byte[] buffer = new byte[1024];
		while (read != -1) {
			read = in.read(buffer);
			if (read != -1) {
				count += read;
				out.write(buffer, 0, read);
			}
		}
		return count;
	}

	public static int copy(Reader input, Writer output) throws IOException 
	{
		char[] buffer = new char[1024];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

}
