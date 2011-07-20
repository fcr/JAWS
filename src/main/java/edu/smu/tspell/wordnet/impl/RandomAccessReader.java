/*

  Java API for WordNet Searching 1.0
  Copyright (c) 2007 by Brett Spell.

  This software is being provided to you, the LICENSEE, by under the following
  license.  By obtaining, using and/or copying this software, you agree that
  you have read, understood, and will comply with these terms and conditions:
   
  Permission to use, copy, modify and distribute this software and its
  documentation for any purpose and without fee or royalty is hereby granted,
  provided that you agree to comply with the following copyright notice and
  statements, including the disclaimer, and that the same appear on ALL copies
  of the software, database and documentation, including modifications that you
  make for internal use or for distribution.

  THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" WITHOUT REPRESENTATIONS OR
  WARRANTIES, EXPRESS OR IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION,  
  LICENSOR MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANTABILITY OR FITNESS
  FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE LICENSED SOFTWARE OR
  DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS,
  TRADEMARKS OR OTHER RIGHTS.

 */
package edu.smu.tspell.wordnet.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Used to access the data at arbitrary positions within a file.
 * 
 * @author Brett Spell
 */
public abstract class RandomAccessReader
{

	/**
	 * Separates the lines within the file.
	 */
	protected final static char LINE_SEPARATOR = '\n';

	/**
	 * Mode in which to open the file (read-only).
	 */
	protected final static String ACCESS_MODE = "r";

	/**
	 * Provides random access to the content of the file being read.
	 */
	protected ByteBuffer accessor;
	
	/**
	 * The file's size in bytes, which is assumed not to change.
	 */
	private int fileSize;

	/**
	 * Pointer to the next byte in the file that will be read.
	 */
	private int filePointer;
	
	/**
	 * Constructs an instance of this class, specifying the file that is to
	 * be read.
	 * 
	 * @param  file File that is to be read.
	 * @throws FileNotFoundException The specified file does not exist.
	 */
	protected RandomAccessReader(String name) throws IOException
	{
		super();
		InputStream stream = getClass().getResourceAsStream(name);
		if (stream == null) {
			throw new IOException("Cannot open resource: " + name);
		}
		fileSize = stream.available();
		byte [] buffer = new byte[(int)fileSize];
		int read = stream.read(buffer, 0, fileSize);
		if (read != fileSize) {
			throw new IOException("Unsuccessful read from: " + name + " " + read + " instead of " + fileSize);			
		}
		accessor = ByteBuffer.wrap(buffer);
		filePointer = accessor.position();
	}


	/**
	 * Moves the file pointer to a new position and records that position so
	 * that it's available for quick retrieval.
	 * 
	 * @param  newPosition New byte position within the file.
	 * @throws IOException An error occurred updating the file pointer.
	 */
	protected synchronized void seek(long newPosition) throws IOException
	{
		if (newPosition != filePointer)
		{
			ByteBuffer reader = getAccessor();
			reader.position((int)newPosition);
			filePointer = (int)newPosition;
		}
	}

	/**
	 * Beginning with the current / next character in the file, read until
	 * we encounter either the line delimiter or the end of the file and
	 * return the characters (excluding the delimiter) that were read.
	 * 
	 * @return Characters read from the file position at entry until a line
	 *         delimiter or the end of the file was encountered.
	 * @throws IOException An error occurred reading the file.
	 */
	protected synchronized String readToNextEndOfLine() throws IOException
	{
		char nextChar;

		StringBuffer buffer = new StringBuffer();
		while (filePointer < fileSize)
		{
			nextChar = readNextCharacter();
			if (nextChar != LINE_SEPARATOR)
			{
				buffer.append(nextChar);
			}
			else
			{
				break;
			}
		}
		return buffer.toString();
	}

	/**
	 * Reads the next sequential character from the file.
	 * 
	 * @return Next character in the file or -1 if reached end-of-file.
	 * @throws IOException An error occurred reading the character.
	 * @see    RandomAccessFile#read()
	 */
	protected synchronized char readNextCharacter() throws IOException
	{
		char nextChar = (char)(-1);
		if (filePointer < fileSize)
		{
			nextChar = (char)accessor.get();
			filePointer++;
		}
		return nextChar;
	}

	/**
	 * Returns a reference to the file reader used to access the file.
	 * 
	 * @return Random access file reader.
	 */
	protected ByteBuffer getAccessor()
	{
		return accessor;
	}

	/**
	 * Returns the number of bytes in the file being read.
	 * 
	 * @return Number of bytes in the file being read.
	 */
	protected long getFileSize()
	{
		return fileSize;
	}

	/**
	 * Returns a reference to the current file pointer value.
	 * 
	 * @return Current file pointer value.
	 */
	protected long getFilePointer()
	{
		return filePointer;
	}

	/**
	 * Called by the garbage collector when it's about to reclaim the memory
	 * associated with this object.
	 * <br><p>
	 * We use this opportunity to close the file that was being read;
	 * otherwise the file would remain open until the JVM exited.
	 */
	protected void finalize() throws Throwable
	{
		accessor = null;
	}

}