/**
 * Copyright © 2015 Martin Scharm <martin@binfalse.de>
 * 
 * This file is part of the DocumentObjectCompiler.
 * 
 * The DocumentObjectCompiler is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * CombineExt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DocumentObjectCompiler. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.unirostock.sems.docompiler;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Unit test for the document object compiler.
 */
public class CompilerTest
{
	
	/** The out content. */
	private final ByteArrayOutputStream	outContent	= new ByteArrayOutputStream ();
	
	/** The err content. */
	private final ByteArrayOutputStream	errContent	= new ByteArrayOutputStream ();
	
	
	/**
	 * Sets the up environment.
	 * 
	 * we'll skip annoying log4j stuff and won't exit in case of "miss usage"..
	 */
	@BeforeClass
	public static void setUpEnv ()
	{
		Logger.getRootLogger ().setLevel (Level.OFF);
		Compiler.DIE = false;
	}
	
	
	/**
	 * Sets the up streams.
	 */
	@Before
	public void setUpStreams ()
	{
		System.setOut (new PrintStream (outContent));
		System.setErr (new PrintStream (errContent));
	}
	
	
	/**
	 * Clean up streams.
	 */
	@After
	public void cleanUpStreams ()
	{
		System.setOut (null);
		System.setErr (null);
	}
	
	
	/**
	 * Test valid file.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testValidFile () throws IOException, InterruptedException
	{
		// some workarounds
		Logger.getRootLogger ().setLevel (Level.OFF);
		Compiler.DIE = false;
		
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.bundle"), target);
		Compiler.main (new String[] { target.toString () });
		
		assertTrue ("expected to obtain a pdf file",
			Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertEquals ("did not expect to see an err", 0, syserr.length ());
		
		String myErrMag = "my message";
		Compiler.die (myErrMag);
		syserr = errContent.toString ();
		assertTrue ("expected to see an error", syserr.length () > 10);
		assertTrue ("expected to see my error",
			syserr.startsWith ("!!!") && syserr.contains (myErrMag));
	}
	
	
	/**
	 * Test invalid bundle having an external reference.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testInvalidBundleExternalRef ()
		throws IOException,
			InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err.bundle"), target);
		Compiler.main (new String[] { target.toString () });
		
		assertFalse ("expected to not see a pdf file",
			Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	
	/**
	 * Test invalid bundle missing the root document annotation.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testInvalidBundleMissingAnnotation ()
		throws IOException,
			InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err2.bundle"), target);
		Compiler.main (new String[] { target.toString () });
		
		assertFalse ("expected to not see a pdf file",
			Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	
	/**
	 * Test invalid bundle containing an invalid tex file.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testInvalidBundleInvalidTex ()
		throws IOException,
			InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err3.bundle"), target);
		Compiler.main (new String[] { target.toString () });
		
		assertFalse ("expected to not see a pdf file",
			Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	
	/**
	 * Test miss usage1.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testMissUsage1 () throws IOException, InterruptedException
	{
		Compiler.main (null);
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	
	/**
	 * Test miss usage2.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testMissUsage2 () throws IOException, InterruptedException
	{
		Compiler.main (new String[] { "test/do.bundle", "test/do.bundle" });
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	
	/**
	 * Test miss usage3.
	 * 
	 * @throws IOException
	 *           the IO exception
	 * @throws InterruptedException
	 *           the interrupted exception
	 */
	@Test
	public void testMissUsage3 () throws IOException, InterruptedException
	{
		Compiler.main (new String[] { "test/do.bundle.does.not.exist" });
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
}
