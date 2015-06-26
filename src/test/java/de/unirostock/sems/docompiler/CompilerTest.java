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
 * Unit test for simple the document object compiler.
 */
public class CompilerTest
{private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

@BeforeClass
public static void setUpEnv () {
	Logger.getRootLogger().setLevel(Level.OFF);
	Compiler.DIE = false;
}
@Before
public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
}

@After
public void cleanUpStreams() {
    System.setOut(null);
    System.setErr(null);
}
	
	@Test
	public void testValidFile () throws IOException, InterruptedException
	{
		// some workarounds
		Logger.getRootLogger().setLevel(Level.OFF);
		Compiler.DIE = false;
		
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.bundle"), target);
		Compiler.main (new String [] {target.toString ()});
		
		assertTrue ("expected to obtain a pdf file", Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertEquals ("did not expect to see an err", 0, syserr.length ());
		
		String myErrMag = "my message";
		Compiler.die (myErrMag);
		syserr = errContent.toString ();
		assertTrue ("expected to see an error", syserr.length () > 10);
		assertTrue ("expected to see my error", syserr.startsWith ("!!!") && syserr.contains (myErrMag));
	}
	
	@Test
	public void testInvalidBundleExternalRef () throws IOException, InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err.bundle"), target);
		Compiler.main (new String [] {target.toString ()});
		
		assertFalse ("expected to not see a pdf file", Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	@Test
	public void testInvalidBundleMissingAnnotaion () throws IOException, InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err2.bundle"), target);
		Compiler.main (new String [] {target.toString ()});
		
		assertFalse ("expected to not see a pdf file", Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	@Test
	public void testInvalidBundleInvalidTex () throws IOException, InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("test/do.err3.bundle"), target);
		Compiler.main (new String [] {target.toString ()});
		
		assertFalse ("expected to not see a pdf file", Files.exists (tmp.resolve ("test.pdf")));
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	@Test
	public void testMissUsage1 () throws IOException, InterruptedException
	{
		Compiler.main (null);
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	@Test
	public void testMissUsage2 () throws IOException, InterruptedException
	{
		Compiler.main (new String [] {"test/do.bundle", "test/do.bundle"});
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
	@Test
	public void testMissUsage3 () throws IOException, InterruptedException
	{
		Compiler.main (new String [] {"test/do.bundle.does.not.exist"});
		String syserr = errContent.toString ();
		assertTrue ("expected an err", 0 < syserr.length ());
	}
	
}
