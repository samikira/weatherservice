package smkra.weather.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import smkra.weather.main.WeatherService;

public class ParameterTest {
	
	private String testFileName = "data/test.html";
	
	@After
	public void deleteTestFile(){
		File testFile = new File(testFileName);
		if(testFile.exists())
			testFile.delete();
	}
	
	@Test
    public void testHelp()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-h"});
	    	Assert.assertEquals("should print help info", baos.toString(),new WeatherService().getUsageHelpString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
	
	@Test
    public void testHelp2()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-h","-c","青森","-f",testFileName});
	    	Assert.assertEquals("should print help info", baos.toString(),new WeatherService().getUsageHelpString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
    
	@Test
    public void testCorrectParam()
    {
    	try {
    		File file = new File(testFileName);
    		Assert.assertFalse("file should not exist", file.exists());
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-c","青森","-f",testFileName});
	    	Assert.assertTrue("should contain requested city", baos.toString().contains("青森"));
	    	file = new File(testFileName);
	    	Assert.assertTrue("file does not exist", file.exists());
	    	Assert.assertTrue("file should contain requested city", testFileContent("青森"));
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
	
	@Test
    public void testCorrectParam2()
    {
    	try {
    		File file = new File(testFileName);
    		Assert.assertFalse("file should not exist", file.exists());
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-c","大津","-f",testFileName,"-d","-r","1","-s","1","-ms","1"});
	    	Assert.assertTrue("should contain requested city", baos.toString().contains("大津"));
	    	file = new File(testFileName);
	    	Assert.assertTrue("file does not exist", file.exists());
	    	Assert.assertTrue("file should contain requested city", testFileContent("大津"));
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
    
	@Test
    public void testWrongParam()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-xy"});
	    	Assert.assertEquals("should print usage info", baos.toString(), WeatherService.getUsageString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
    
	@Test
    public void testWrongParam2()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-c","青森","-f",testFileName,"-bla"});
	    	Assert.assertEquals("should print usage info", baos.toString(), WeatherService.getUsageString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
    
	@Test
    public void testWrongParam3()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-c","青森"});
	    	Assert.assertEquals("should print usage info", baos.toString(), WeatherService.getUsageString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
    
	@Test
    public void testWrongParam4()
    {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-f",testFileName});
	    	Assert.assertEquals("should print usage info", baos.toString(), WeatherService.getUsageString()+"\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
    }
	
	@Test
	public void testWrongCity() {
		try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		PrintStream ps = new PrintStream(baos, true, "utf-8");
    		WeatherService.outStream = ps;
	    	WeatherService.main(new String[]{"-c","ウィーン","-f",testFileName});
	    	Assert.assertEquals("should print usage info", baos.toString(), "Could not get weather information for specified city.\n");
    	} catch (Exception e) {
    		Assert.fail("no exception should be thrown"+e.getMessage());
    	}
	}
    
    private boolean testFileContent(String includedString) throws IOException {
    	String content = new String ( Files.readAllBytes( Paths.get(testFileName)));
    	return content.contains(includedString);
    }
    
}
