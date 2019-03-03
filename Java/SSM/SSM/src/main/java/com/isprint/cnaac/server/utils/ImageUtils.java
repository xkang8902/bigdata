package com.isprint.cnaac.server.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


@SuppressWarnings("restriction")
public class ImageUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(ImageUtils.class);
 
	public static String encodeImgageToBase64(URL imageUrl) {
	    ByteArrayOutputStream outputStream = null;
	    try {
	      BufferedImage bufferedImage = ImageIO.read(imageUrl);
	      outputStream = new ByteArrayOutputStream();
	      ImageIO.write(bufferedImage, "jpg", outputStream);
	    } catch (MalformedURLException e1) {
	    	logger.error("can not get image form this url:" + imageUrl + " exception: " + e1.getMessage());
	    } catch (IOException e) {
	    	logger.error("can not get image form this url:" + imageUrl + " exception: " + e.getMessage());
	    }
	    
	    BASE64Encoder encoder = new BASE64Encoder();
	    return encoder.encode(outputStream.toByteArray());
	  }
	
	  
	public static String encodeImgageToBase64(File imageFile) {
	    ByteArrayOutputStream outputStream = null;
	    try {
	      BufferedImage bufferedImage = ImageIO.read(imageFile);
	      outputStream = new ByteArrayOutputStream();
	      ImageIO.write(bufferedImage, "jpg", outputStream);
	      BASE64Encoder encoder = new BASE64Encoder();
		  return encoder.encode(outputStream.toByteArray());
	    } catch (Exception e) {
	    	logger.error("can not get image form this file:" + imageFile + " exception: " + e.getMessage());
	    	return null;
	    } 	   
	    
	  }
	
	  /**
	   * 将Base64位编码的图片进行解码，并保存到指定目录   * 
	   */
	public static void decodeBase64ToImage(String base64, String path,
	      String imgName) {
	    BASE64Decoder decoder = new BASE64Decoder();
	    try {
	      FileOutputStream write = new FileOutputStream(new File(path
	          + imgName));
	      byte[] decoderBytes = decoder.decodeBuffer(base64);
	      write.write(decoderBytes);
	      write.close();
	    } catch (IOException e) {
	    	logger.error("save file to this folder error: " + path + " exception: " + e.getMessage());
	    }
	  }
}