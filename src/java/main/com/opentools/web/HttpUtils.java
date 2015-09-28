package com.opentools.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.opentools.charset.CharsetUtil;

/**
 * HTTP POST和GET处理工具类
 * @author Aaron
 * @date 2013-12-18 11:22
 */
public class HttpUtils {
	
	/**
     * 向指定URL发送GET方法的请求
     * @param url 发送请求的URL
     * @param param 请求参数
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
     * @param param 请求参数
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
    	multipartEntityBuilder.setCharset(Charset.forName(CharsetUtil.UTF_8));
    	HttpEntity httpEntity = multipartEntityBuilder.build();
    	httpPost.setEntity(httpEntity);
    	CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
    	return response;
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
	
	public static void main(String[] args) {
		
		Map<String, File> mapFile = new LinkedHashMap<>();
		File file = new File("D://开发版_20150923.zip");
		String replaceAll = UUID.randomUUID().toString().replaceAll("-", "");
		System.out.println(replaceAll);
		mapFile.put("pjr" + replaceAll, file);
		
		Map<String, String> maps = new LinkedHashMap<>();
		try {
			CloseableHttpResponse response = HttpUtils.sendFile("http://192.168.1.43:8080/upload/fileupload", maps, mapFile);
			Header[] headers = response.getAllHeaders();
			for (Header header : headers)
			{
				System.out.println(header.getName() + "=====" + header.getValue());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}