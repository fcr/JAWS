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
package edu.smu.tspell.wordnet.impl.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Names of system properties used by the application.
 * 
 * @author Brett Spell
 */
public class PropertyNames
{
	
	/**
	 * Identifies the location of the database directory.
	 */
	private final static String DATABASE_DIRECTORY = "wordnet.database.dir";
	public final static String databaseDirectory;

	/**
	 * If true use the native word forms form (compound words joined by underscores).
	 */
	private final static String WORD_FORMAT = "wordnet.database.nativeFormat";
	public final static boolean nativeFormat;

	/**
	 * If true asume that all queries will be single words.
	 */
	private final static String SINGLE_WORD_QUERIES = "wordnet.database.singleWordQueries";
	public final static boolean singleWordQueries;

	static {
		Properties props = new Properties();
		String propertyValue = "";

		// try retrieve data from file
		try {
			InputStream input = PropertyNames.class.getResourceAsStream("/wordnet.properties");
			props.load(input);

			propertyValue = props.getProperty(DATABASE_DIRECTORY);
			databaseDirectory = propertyValue;
			propertyValue = props.getProperty(WORD_FORMAT);
			nativeFormat = propertyValue.toLowerCase().equals("true");
			propertyValue = props.getProperty(SINGLE_WORD_QUERIES);
			singleWordQueries = propertyValue.toLowerCase().equals("true");
			input.close();
		}
		// catch exception in case properties file does not exist
		catch (IOException e) {
			String msg = "'" + propertyValue + "' is an invalid.";
	        throw new Error(msg, e);
		}
	}
	
	
	/**
	 * No-argument constructor.
	 */
	private PropertyNames()
	{
	}


}