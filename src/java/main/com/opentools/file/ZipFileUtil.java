package com.opentools.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * 压缩和解压缩文件相关的类,实际上war文件和zip文件使用的是同样的压缩算法
 * 
 * @author Aaron
 * 
 */
public class ZipFileUtil {

	public static final String EXT = ".zip";

	private static final int BUFFER = 1024;

	public static void main(String[] args) {
		
		ZipFileUtil zipFileUtil = new ZipFileUtil();
		
		String directory = "D:" + File.separator + "gent_login";
		String destFile = "D:" + File.separator + "aa.zip";
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile));
			zipFileUtil.startCompress(zos, "", directory);
			zos.flush();
			zos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startCompress(ZipOutputStream zos, String oppositePath, String directory) {
		
		File file = new File(directory);
		if (file.isDirectory()) {
			// 如果是压缩目录
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File aFile = files[i];
				if (aFile.isDirectory()) {
					// 如果是目录，修改相对地址
					String newOppositePath = oppositePath + aFile.getName() + File.separator;
					// 创建目录
					compressDirectory(zos, oppositePath, aFile);
					// 进行递归调用
					startCompress(zos, newOppositePath, aFile.getPath());
				} else {
					// 如果不是目录，则进行压缩
					compressFile(zos, oppositePath, aFile);
				}
			}
		} else {
			// 如果是压缩文件，直接调用压缩方法进行压缩
			compressFile(zos, oppositePath, file);
		}
	}

	public void compressFile(ZipOutputStream zos, String oppositePath, File file) {
		// 创建一个Zip条目，每个Zip条目都是必须相对于根路径
		ZipEntry entry = new ZipEntry(oppositePath + file.getName());
		InputStream is = null;
		try {
			// 将条目保存到Zip压缩文件当中
			zos.putNextEntry(entry);
			// 从文件输入流当中读取数据，并将数据写到输出流当中.
			is = new FileInputStream(file);
			IOUtils.write(IOUtils.toByteArray(is), zos);
			zos.closeEntry();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void compressDirectory(ZipOutputStream zos, String oppositePath, File file) {
		// 压缩目录，这是关键，创建一个目录的条目时，需要在目录名后面加多一个"/"
		ZipEntry entry = new ZipEntry(oppositePath + file.getName() + File.separator);
		try {
			zos.putNextEntry(entry);
			zos.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 文件 解压缩
	 * 
	 * @param srcPath
	 *            源文件路径
	 * 
	 * @throws Exception
	 */
	public static void decompress(String srcPath) throws Exception {

		File srcFile = new File(srcPath);
		decompress(srcFile);
	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @throws Exception
	 */
	public static void decompress(File srcFile) throws Exception {
		String basePath = srcFile.getParent();
		decompress(srcFile, basePath);
	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @param destFile
	 * @throws Exception
	 */
	public static void decompress(File srcFile, File destFile) throws Exception {

		CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
				srcFile), new CRC32());

		ZipInputStream zis = new ZipInputStream(cis);

		decompress(destFile, zis);

		zis.close();

	}

	/**
	 * 解压缩
	 * 
	 * @param srcFile
	 * @param destPath
	 * @throws Exception
	 */
	public static void decompress(File srcFile, String destPath)
			throws Exception {

		decompress(srcFile, new File(destPath));

	}

	/**
	 * 文件 解压缩
	 * 
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            目标文件路径
	 * @throws Exception
	 */
	public static void decompress(String srcPath, String destPath)
			throws Exception {

		File srcFile = new File(srcPath);
		decompress(srcFile, destPath);
	}

	/**
	 * 文件 解压缩
	 * 
	 * @param destFile
	 *            目标文件
	 * @param zis
	 *            ZipInputStream
	 * @throws Exception
	 */
	private static void decompress(File destFile, ZipInputStream zis)
			throws Exception {

		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {

			// 文件
			String dir = destFile.getPath() + File.separator + entry.getName();

			File dirFile = new File(dir);

			// 文件检查
			fileProber(dirFile);

			if (entry.isDirectory()) {

				dirFile.mkdirs();
			} else {

				decompressFile(dirFile, zis);
			}

			zis.closeEntry();
		}
	}

	/**
	 * 文件探针
	 * 
	 * 
	 * 当父目录不存在时，创建目录！
	 * 
	 * 
	 * @param dirFile
	 */
	private static void fileProber(File dirFile) {

		File parentFile = dirFile.getParentFile();
		if (!parentFile.exists()) {

			// 递归寻找上级目录
			fileProber(parentFile);

			parentFile.mkdir();
		}

	}

	/**
	 * 文件解压缩
	 * 
	 * @param destFile
	 *            目标文件
	 * @param zis
	 *            ZipInputStream
	 * @throws Exception
	 */
	private static void decompressFile(File destFile, ZipInputStream zis)
			throws Exception {

		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(destFile));

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = zis.read(data, 0, BUFFER)) != -1) {

			bos.write(data, 0, count);
		}

		bos.close();
	}

	private ZipFileUtil() {
	}
}