package nl.nn.adapterframework.pipes;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

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
	public void testChecksumResultADLER32() throws PipeRunException {
		pipe.setType(CHECKSUM_ADLER32);
		String result = (String)pipe.doPipe("dummy string", session).getResult();

		assertEquals("1fbe04e4", result);
	}

	@Test
	public void testChecksumResultCRC32() throws PipeRunException {
		pipe.setType(CHECKSUM_CRC32);
		String result = (String)pipe.doPipe("dummy string", session).getResult();

		assertEquals("51f26b7", result);
	}

	@Test
	public void testChecksumResultMD5() throws PipeRunException {
		pipe.setType(CHECKSUM_MD5);
		String result = (String)pipe.doPipe("dummy string", session).getResult();

		assertEquals("ec7dd4aab5611a76a24985bf59976763", result);
	}

	@Test
	public void testChecksumResultSHA() throws PipeRunException {
		pipe.setType(CHECKSUM_SHA);
		String result = (String)pipe.doPipe("dummy string", session).getResult();

		assertEquals("2883565e30019b2ece774e02e6e2da38ffefdda5", result);
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
