package com.opentools.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;

import com.opentools.charset.CharsetUtil;

/**
 * 处理http请求相关的工具类
 * @author aaron
 *
 */
public class ServletUtil {

	/**
	 * 上传文件,利用servlet3.1的特性做文件上传处理
	 * @param request
	 * @param path 相对当前项目的文件路径
	 */
	public static void UploadFile(final HttpServletRequest request, String path, final HttpServletResponse response) {
		
		String realPath = request.getServletContext().getRealPath("");
		
		try {
			
			Collection<Part> parts = request.getParts();
			if (null != parts && !parts.isEmpty()) {
				
				for (Part part : parts) {
					
					String fileName = part.getSubmittedFileName();
					File filePath = new File(realPath + File.separator + path);
					if (!filePath.exists()) filePath.mkdirs();
					
					InputStream inputStream = part.getInputStream();
					FileOutputStream fos = new FileOutputStream(realPath + File.separator + path + File.separator + fileName);
					byte[] data = IOUtils.toByteArray(inputStream);
					IOUtils.write(data, fos);
					
					inputStream.close();
					fos.flush();
					fos.close();
				}
			}
			
			response.setContentType(ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), Charset.forName(CharsetUtil.UTF_8)).getMimeType());
			String s = new String("{\"key\":\"value\"}");
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.write(s.getBytes("UTF-8"), outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
	}
	
	private ServletUtil() {}
	
}