package de.unirostock.sems.docompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;



/**
 * Unit test for simple the document object compiler.
 */
public class CompilerTest
{
	
	@Test
	public void testCompiler () throws IOException, InterruptedException
	{
		// copy the test archive to a temp dir
		Path tmp = Files.createTempDirectory ("docompiler");
		Path target = tmp.resolve ("test.zip");
		Files.copy (Paths.get ("/tmp/text.bundle"), target);
		Compiler.main (new String [] {target.toString ()});
	}
	
}
