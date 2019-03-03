package com.baidu.ueditor.upload;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.UeditorConstants;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public final class Base64Uploader {

	public static State save(String content, Map<String, Object> conf) {
		
		byte[] data = decode(content);

		long maxSize = ((Long) conf.get("maxSize")).longValue();

		if (!validSize(data, maxSize)) {
			return new BaseState(false, AppInfo.MAX_SIZE);
		}

		String suffix = FileType.getSuffix("JPG");

		String savePath = PathFormat.parse((String) conf.get("savePath"),
				(String) conf.get("filename"));
		
		savePath = savePath + suffix;
		String physicalPath = (String) conf.get("rootPath") + savePath;
		
		if (UeditorConstants.CUSTOMIZE_UEDITOR){
			physicalPath = savePath;
		}

		State storageState = StorageManager.saveBinaryFile(data, physicalPath);

		if (storageState.isSuccess()) {
			String url = PathFormat.format(savePath);
			if( UeditorConstants.CUSTOMIZE_UEDITOR ){
				url = savePath.replace(UeditorConstants.PUBLIC_FOLDER.replace("\\", "/"), UeditorConstants.PUBLIC_STATIC_WEB_SITE);
			}
			storageState.putInfo("url", url);
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", "");
		}

		return storageState;
	}

	private static byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private static boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}
	
}