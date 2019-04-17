package nl.nn.adapterframework.pipes;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.core.IPipeLineSession;
import nl.nn.adapterframework.core.PipeLineSessionBase;
import nl.nn.adapterframework.core.PipeRunException;
import nl.nn.adapterframework.core.PipeRunResult;
import nl.nn.adapterframework.pipes.ChecksumPipe.ChecksumGenerator;
import nl.nn.adapterframework.pipes.ChecksumPipe.MessageDigestChecksumGenerator;
import nl.nn.adapterframework.pipes.ChecksumPipe.ZipChecksumGenerator;



public class ChecksumPipeTest extends PipeTestBase<ChecksumPipe> {

	public static final String CHECKSUM_MD5="MD5";
	public static final String CHECKSUM_SHA="SHA";
	public static final String CHECKSUM_CRC32="CRC32";
	public static final String CHECKSUM_ADLER32="Adler32";
	private IPipeLineSession session = new PipeLineSessionBase();

	@Override
	public ChecksumPipe createPipe() {
		return new ChecksumPipe();
	}
	
	// TEST FOR CONFIGURED CHECKSUM TYPES
	
	@Test(expected = ConfigurationException.class)
	public void testEmptyChecksumType() throws ConfigurationException {
		String type = "";
		pipe.setType(type);
		pipe.configure();
	}
	
	@Test(expected = ConfigurationException.class)
	public void testNullChecksumType() throws ConfigurationException {
		pipe.setType(null);
		pipe.configure();
	}
	
	@Test(expected = ConfigurationException.class)
	public void testInvalidChecksumType() throws ConfigurationException {
		pipe.setType("dummy");
		pipe.configure();
	}
	
	@Test
	public void testChecksumTypeADLER32() throws ConfigurationException {
		pipe.setType(CHECKSUM_ADLER32);
		pipe.configure();
		assertEquals(CHECKSUM_ADLER32, pipe.getType());
	}
	
	@Test
	public void testChecksumTypeCRC32() throws ConfigurationException {
		pipe.setType(CHECKSUM_CRC32);
		pipe.configure();
		assertEquals(CHECKSUM_CRC32, pipe.getType());
	}
	
	@Test
	public void testChecksumTypeMD5() throws ConfigurationException {
		pipe.setType(CHECKSUM_MD5);
		pipe.configure();
		assertEquals(CHECKSUM_MD5, pipe.getType());
	}
	
	@Test
	public void testChecksumTypeSHA() throws ConfigurationException {		
		pipe.setType(CHECKSUM_SHA);
		pipe.configure();
		assertEquals(CHECKSUM_SHA, pipe.getType());
	}
	
	// TEST FOR CHECKSUM GENERATOR TYPES
	
	@Test(expected = NoSuchAlgorithmException.class)
	public void testEmptyGeneratorType() throws NoSuchAlgorithmException {
		pipe.setType("");
		pipe.createChecksumGenerator();
	}
	
	@Test
	public void testGeneratorTypeADLER32() throws NoSuchAlgorithmException {
		pipe.setType(CHECKSUM_ADLER32);
		ChecksumGenerator result = pipe.createChecksumGenerator();

		assertTrue(result instanceof ZipChecksumGenerator);
	}

	@Test
	public void testGeneratorTypeCRC32() throws NoSuchAlgorithmException {
		pipe.setType(CHECKSUM_CRC32);
		ChecksumGenerator result = pipe.createChecksumGenerator();

		assertTrue(result instanceof ZipChecksumGenerator);
	}

	@Test
	public void testGeneratorTypeMD5() throws NoSuchAlgorithmException {
		pipe.setType(CHECKSUM_MD5);
		ChecksumGenerator result = pipe.createChecksumGenerator();

		assertTrue(result instanceof MessageDigestChecksumGenerator);
	}
	
	@Test
	public void testGeneratorTypeSHA() throws NoSuchAlgorithmException {
		pipe.setType(CHECKSUM_SHA);
		ChecksumGenerator result = pipe.createChecksumGenerator();

		assertTrue(result instanceof MessageDigestChecksumGenerator);
	}
	
	// TEST FOR CORRECT AND CONSISTENT CHECKSUM RESULTS

	@Test
	public void testChecksumResultADLER32_Unix() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_ADLER32);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testUnix.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("cae22763", result);
	}
	
	@Test
	public void testChecksumResultADLER32_Windows() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_ADLER32);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testWindows.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("f9c2277d", result);
	}
	
	@Test
	public void testChecksumResultCRC32_Unix() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_CRC32);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testUnix.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("cb67c762", result);
	}
	
	@Test
	public void testChecksumResultCRC32_Windows() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_CRC32);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testWindows.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("b31c1921", result);
	}
	
	@Test
	public void testChecksumResultMD5_Unix() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_MD5);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testUnix.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("2ce5de996eb02123131814426c2423ca", result);
	}
	
	@Test
	public void testChecksumResultMD5_Windows() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_MD5);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testWindows.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("e26cfe84eccc86aa4f1189195dab9711", result);
	}
	
	@Test
	public void testChecksumResultSHA_Unix() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_SHA);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testUnix.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("961a501b131479a778abe5fe483f86b4727c9ce1", result);
	}
	
	@Test
	public void testChecksumResultSHA_Windows() throws PipeRunException, IOException {
		pipe.setType(CHECKSUM_SHA);
		
		String input = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("ChecksumTest/testWindows.xml").getFile()));
		String result = (String)pipe.doPipe(input, session).getResult();
		assertEquals("2770773f1cca9fe6389da30100184f89816de1a3", result);
	}
	
	// MISCELLANEOUS TESTS
	
	@Test(expected = PipeRunException.class)
	public void cantCalculate() throws PipeRunException {
		pipe.doPipe(null, session);
	}

	@Test(expected = PipeRunException.class)
	public void wrongPathToFile() throws PipeRunException {
		pipe.setInputIsFile(true);
		pipe.doPipe("dummyPathToFile", session);
	}

	@Test(expected = PipeRunException.class)
	public void badCharset() throws PipeRunException {
		pipe.setInputIsFile(false);
		pipe.setCharset("dummy");
		pipe.doPipe("anotherDummy", session);
	}

	@Test
	public void emptyCharset() throws PipeRunException {
		pipe.setInputIsFile(false);
		pipe.setCharset("");
		assertNotNull(pipe.doPipe("anotherDummy", session));
	}

}
