package com.opentools.web;

import com.opentools.charset.CharSetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * HTTP POST和GET处理工具类
 * @author Aaron
 * @date 2013-12-18 11:22
 */
public class HttpUtils {
	
	/**
     * 向指定URL发送GET方法的请求
     * @param url 发送请求的URL
     * @param params 请求参数
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, HashMap<String,String> params) {
        String result = "";
        BufferedReader in = null;
        try {
        	/**组装参数**/
        	String param = parseParams(params);
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            /**打开和URL之间的连接**/
            URLConnection connection = realUrl.openConnection();
            /**设置通用的请求属性**/
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            /**建立实际的连接**/
            connection.connect();
            /**定义 BufferedReader输入流来读取URL的响应**/
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {/**使用finally块来关闭输入流**/
            try {
                if(in != null) { in.close(); }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * @param url 发送请求的 URL
     * @param params 请求参数
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, HashMap<String,String> params) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            /**打开和URL之间的连接**/
            URLConnection conn = realUrl.openConnection();
            /**设置通用的请求属性**/
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            /**发送POST请求必须设置如下两行**/
            conn.setDoOutput(true);
            conn.setDoInput(true);
            /**获取URLConnection对象对应的输出流**/
            out = new PrintWriter(conn.getOutputStream());
            /**发送请求参数**/
            String param = parseParams(params);
            out.print(param);
            /**flush输出流的缓冲**/
            out.flush();
            /**定义BufferedReader输入流来读取URL的响应**/
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        } finally{ /**使用finally块来关闭输出流、输入流**/
            try{
                if(out!=null){   out.close();}
                if(in!=null){ in.close(); }
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    /**
     * 文件上传的工具类
     * @param url,接收请求的地址
     * @param map 请求的参数
     * @param mapFile 发送的文件
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static CloseableHttpResponse sendFile(String url, Map<String, String> map, Map<String, File> mapFile) throws ClientProtocolException, IOException
    {
    	CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
    	HttpPost httpPost = new HttpPost(url);
    	MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    	if (null != mapFile && !mapFile.isEmpty())
    	{
    		
    		Set<String> keySet = mapFile.keySet();
        	
        	for (String key : keySet)
        	{
        		File file = mapFile.get(key);
        		if (null != file && file.exists())
        		{
        			FileBody fileBody = new FileBody(mapFile.get(key));
        			multipartEntityBuilder.addPart(key, fileBody);
        		}
        	}
    	}
    	
    	if (null != map && !map.isEmpty())
    	{
    		Set<String> keySet = map.keySet();
    		for (String key : keySet)
    		{
    			String value = map.get(key);
    			if (null != value && !value.isEmpty())
    			{
    				multipartEntityBuilder.addTextBody(key, value);
    			}
    		}
    	}
    	multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    	multipartEntityBuilder.setCharset(Charset.forName(CharSetUtil.UTF_8));
    	HttpEntity httpEntity = multipartEntityBuilder.build();
    	httpPost.setEntity(httpEntity);
    	CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
    	return response;
    }
    
    /**
     * 发送携带header和参数的文件
     * @param url
     * @param headers
     * @param params
     * @param files
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public static CloseableHttpResponse sendPost(String url, Map<String, String> headers, Map<String, String> params, Map<String, File> files) throws ClientProtocolException, IOException {
    	
    	CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
    	HttpPost httpPost = new HttpPost(url);
    	MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
    	
    	if (null != headers && !headers.isEmpty()) {
    		
    		Set<String> keySet = headers.keySet();
    		
    		for (String key : keySet) {
    			
    			httpPost.addHeader(key, headers.get(key));
    			
    		}
    	}
    	
    	if (null != files && !files.isEmpty())
    	{
    		
    		Set<String> keySet = files.keySet();
        	
        	for (String key : keySet)
        	{
        		File file = files.get(key);
        		if (null != file && file.exists())
        		{
        			FileBody fileBody = new FileBody(files.get(key));
        			multipartEntityBuilder.addPart(key, fileBody);
        		}
        	}
    	}
    	
    	if (null != params && !params.isEmpty())
    	{
    		Set<String> keySet = params.keySet();
    		for (String key : keySet)
    		{
    			String value = params.get(key);
    			if (null != value && !value.isEmpty())
    			{
    				multipartEntityBuilder.addTextBody(key, value, ContentType.DEFAULT_TEXT);
    			}
    		}
    	}
    	multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    	multipartEntityBuilder.setCharset(Charset.forName(CharSetUtil.UTF_8));
    	HttpEntity httpEntity = multipartEntityBuilder.build();
    	httpPost.setEntity(httpEntity);
    	CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
    	return response;
    }
    
    /**
     * jdk原生方法get方式的请求
     * @param url
     * @return
     */
    public static List<String> Get(String url) {
    	
		try {
			
			URL getUrl = new URL(url);
			URLConnection connection = getUrl.openConnection();
			HttpURLConnection urlConnection = (HttpURLConnection) connection;
			urlConnection.setRequestMethod("GET");
			InputStream is = (InputStream) connection.getContent();
			List<String> lines = IOUtils.readLines(is);
			return lines;
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}		
	}
    
    /**
     * post方式的请求，参数会处理成key=value形式
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static List<String> Post(String url, Map<String, String> headers, Map<String, String> params) {
		
		try {
			
			String param = null;
			
			if (null != params && !params.isEmpty()) {
				
				StringBuilder builder = new StringBuilder();
				
				Set<String> keySet = params.keySet();
				List<String> keys = new ArrayList<>(keySet);
				for (String key : keys) {
					
					builder.append(key + "=" + params.get(key));
					if (!key.equals(keys.get(keys.size() - 1))) {
						
						builder.append("&");
					}
				}
				
				param = builder.toString();
			}
			
			URL postUrl = new URL(url);
			URLConnection connection = postUrl.openConnection();
			HttpURLConnection urlConnection = (HttpURLConnection) connection;
			urlConnection.setRequestMethod("POST");
			
			if (null != headers && !headers.isEmpty()) {
				
				Set<String> keySet = headers.keySet();
				
				for (String key : keySet) {
					
					connection.addRequestProperty(key, headers.get(key));
				}
			}
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			if (null != param) {
				
				byte[] data = param.getBytes(Charset.forName("UTF-8"));
				OutputStream stream = connection.getOutputStream();
				IOUtils.write(data, stream);
				stream.flush();
				stream.close();
			}
			
			connection.connect();
			
			InputStream stream = connection.getInputStream();
			
			List<String> lines = IOUtils.readLines(stream);
			
			stream.close();
			
			return lines;
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * post方式发送json格式的数据
	 * @param url
	 * @param json
     * @return
     */
	public static List<String> Post(String url, String json) throws Exception {

		URL postUrl = new URL(url);
		URLConnection connection = postUrl.openConnection();
		HttpURLConnection urlConnection = (HttpURLConnection) connection;
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		OutputStream outputStream = urlConnection.getOutputStream();
		IOUtils.write(json.getBytes(Charset.forName("UTF-8")), outputStream);
		outputStream.flush();
		outputStream.close();
		urlConnection.connect();
		InputStream inputStream = urlConnection.getInputStream();
		List<String> lines = IOUtils.readLines(inputStream, Charset.forName("UTF-8"));
		return lines;
	}

    /**
	 * 多文件上传,以模拟表单提交方式上传文件
	 * @param files
	 * @param url
	 * @return
	 */
	public static List<String> postUpload(List<String> files, String url, Map<String, String> params) {
		
		String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
		
		try {
			
			URL postUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			
			if (null != params && !params.isEmpty()) {
				
				Set<String> keySet = params.keySet();
				
				for (String key : keySet) {
					conn.setRequestProperty(key, params.get(key));
				}
			}
			
			OutputStream out = new DataOutputStream(conn.getOutputStream());
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
			
			for (String filePath : files) {
				
				File file = new File(filePath);
				StringBuilder sb = new StringBuilder();
				sb.append("--");
				sb.append(BOUNDARY);
				sb.append("\r\n");
				sb.append("Content-Disposition: form-data;name=\"file"+ file.getName() +"\";filename=\""+ file.getName() + "\"\r\n");
				sb.append("Content-Type:application/octet-stream\r\n\r\n");
				byte[] data = sb.toString().getBytes();
				out.write(data);
				DataInputStream in = new DataInputStream(new FileInputStream(file));
				int bytes = 0;
				byte[] bufferOut = new byte[1024];
				while ((bytes = in.read(bufferOut)) != -1) {
					out.write(bufferOut, 0, bytes);
				}
				out.write("\r\n".getBytes()); //多个文件时，二个文件之间加入这个
				in.close();
			}
			
			out.write(end_data);
			out.flush();
			out.close();
			
			InputStream stream = conn.getInputStream();
			List<String> lines = IOUtils.readLines(stream);
			stream.close();
			return lines;
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}		
	}
    
    /**
     * 将HashMap参数组装成字符串
     * @param map
     * @return
     */
	private static String parseParams(HashMap<String,String> map){
    	StringBuffer sb = new StringBuffer();
    	if(map != null){
	    	for (Entry<String, String> e : map.entrySet()) {
		    	sb.append(e.getKey());
		    	sb.append("=");
		    	sb.append(e.getValue());
		    	sb.append("&");
	    	}
	    	sb.substring(0, sb.length() - 1);
    	}
    	return sb.toString();
    }
}