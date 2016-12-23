package com.opentools.image;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Aaron
 * @date 2016年12月20日 下午7:14:31
 * @classname ImageUtil.java
 */
public class ImageUtil {

    public static String IMAGE_TYPE_GIF = "gif";// 图形交换格式
    public static String IMAGE_TYPE_JPG = "jpg";// 联合照片专家组
    public static String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
    public static String IMAGE_TYPE_BMP = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
    public static String IMAGE_TYPE_PNG = "png";// 可移植网络图形
    public static String IMAGE_TYPE_PSD = "psd";// Photoshop的专用格式Photoshop

    public static void main(String[] args) {

        // 1-缩放图像：
        // 方法一：按比例缩放
        //ImageUtil.scale("e:/abc.jpg", "e:/abc_scale.jpg", 2, true);// 测试OK
        // 方法二：按高度和宽度缩放
        //ImageUtil.scale2("e:/abc.jpg", "e:/abc_scale2.jpg", 500, 300, true);// 测试OK

        // 2-切割图像：
        // 方法一：按指定起点坐标和宽高切割
        //ImageUtil.cut("e:/abc.jpg", "e:/abc_cut.jpg", 0, 0, 400, 400);// 测试OK
        // 方法二：指定切片的行数和列数
        //ImageUtil.cut2("e:/abc.jpg", "e:/", 2, 2);// 测试OK
        // 方法三：指定切片的宽度和高度
        //ImageUtil.cut3("e:/abc.jpg", "e:/", 300, 300);// 测试OK

        // 3-图像类型转换：
        //ImageUtil.convert("e:/abc.jpg", "GIF", "e:/abc_convert.gif");// 测试OK

        // 4-彩色转黑白：
        //ImageUtil.gray("e:/abc.jpg", "e:/abc_gray.jpg");// 测试OK

        // 5-给图片添加文字水印：
        // 方法一：
        //ImageUtil.pressText("我是水印文字", "e:/abc.jpg", "e:/abc_pressText.jpg", "宋体", Font.BOLD, Color.white, 80, 0, 0, 0.5f);// 测试OK
        // 方法二：
        //ImageUtil.pressText2("我也是水印文字", "e:/abc.jpg", "e:/abc_pressText2.jpg", "黑体", 36, Color.white, 80, 0, 0, 0.5f);// 测试OK

        // 6-给图片添加图片水印：
        //ImageUtil.pressImage("e:/abc2.jpg", "e:/abc.jpg", "e:/abc_pressImage.jpg", 0, 0, 0.5f);// 测试OK
    }

    public final static void scale(String srcImageFile, String result, int scale, boolean flag) {
        try {
            BufferedImage src = ImageIO.read(new File(srcImageFile)); // 读入文件
            int width = src.getWidth(); // 得到源图宽
            int height = src.getHeight(); // 得到源图长
            if (flag) {// 放大
                width = width * scale;
                height = height * scale;
            } else {// 缩小
                width = width / scale;
                height = height / scale;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            ImageIO.write(tag, "JPEG", new File(result));// 输出到文件流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static void scale2(String srcImageFile, String result, int height, int width, boolean bb) {
        try {
            double ratio = 0.0; // 缩放比例
            File f = new File(srcImageFile);
            BufferedImage bi = ImageIO.read(f);
            Image itemp = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            // 计算比例
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (new Integer(height)).doubleValue() / bi.getHeight();
                } else {
                    ratio = (new Integer(width)).doubleValue() / bi.getWidth();
                }
                AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
                itemp = op.filter(bi, null);
            }
            if (bb) {// 补白
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                if (width == itemp.getWidth(null))
                    g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null),
                            itemp.getHeight(null), Color.white, null);
                else
                    g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null),
                            itemp.getHeight(null), Color.white, null);
                g.dispose();
                itemp = image;
            }
            ImageIO.write((BufferedImage) itemp, "JPEG", new File(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static void cut(String srcImageFile, String result, int x, int y, int width, int height) {
        try {
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit()
                        .createImage(new FilteredImageSource(image.getSource(), cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
                g.dispose();
                // 输出为文件
                ImageIO.write(tag, "JPEG", new File(result));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void cut2(String srcImageFile, String descDir, int rows, int cols) {
        try {
            if (rows <= 0 || rows > 20)
                rows = 2; // 切片行数
            if (cols <= 0 || cols > 20)
                cols = 2; // 切片列数
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int destWidth = srcWidth; // 每张切片的宽度
                int destHeight = srcHeight; // 每张切片的高度
                // 计算切片的宽度和高度
                if (srcWidth % cols == 0) {
                    destWidth = srcWidth / cols;
                } else {
                    destWidth = (int) Math.floor(srcWidth / cols) + 1;
                }
                if (srcHeight % rows == 0) {
                    destHeight = srcHeight / rows;
                } else {
                    destHeight = (int) Math.floor(srcWidth / rows) + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit()
                                .createImage(new FilteredImageSource(image.getSource(), cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();
                        // 输出为文件
                        ImageIO.write(tag, "JPEG", new File(descDir + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void cut3(String srcImageFile, String descDir, int destWidth, int destHeight) {
        try {
            if (destWidth <= 0)
                destWidth = 200; // 切片宽度
            if (destHeight <= 0)
                destHeight = 150; // 切片高度
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > destWidth && srcHeight > destHeight) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int cols = 0; // 切片横向数量
                int rows = 0; // 切片纵向数量
                // 计算切片的横向和纵向数量
                if (srcWidth % destWidth == 0) {
                    cols = srcWidth / destWidth;
                } else {
                    cols = (int) Math.floor(srcWidth / destWidth) + 1;
                }
                if (srcHeight % destHeight == 0) {
                    rows = srcHeight / destHeight;
                } else {
                    rows = (int) Math.floor(srcHeight / destHeight) + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit()
                                .createImage(new FilteredImageSource(image.getSource(), cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();
                        // 输出为文件
                        ImageIO.write(tag, "JPEG", new File(descDir + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void convert(String srcImageFile, String formatName, String destImageFile) {
        try {
            File f = new File(srcImageFile);
            f.canRead();
            f.canWrite();
            BufferedImage src = ImageIO.read(f);
            ImageIO.write(src, formatName, new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void gray(String srcImageFile, String destImageFile) {
        try {
            BufferedImage src = ImageIO.read(new File(srcImageFile));
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp op = new ColorConvertOp(cs, null);
            src = op.filter(src, null);
            ImageIO.write(src, "JPEG", new File(destImageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static void pressText(String pressText, String srcImageFile, String destImageFile, String fontName,
                                       int fontStyle, Color color, int fontSize, int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 在指定坐标绘制水印文字
            g.drawString(pressText, (width - (getLength(pressText) * fontSize)) / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// 输出到文件流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void pressText2(String pressText, String srcImageFile, String destImageFile, String fontName,
                                        int fontStyle, Color color, int fontSize, int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 在指定坐标绘制水印文字
            g.drawString(pressText, (width - (getLength(pressText) * fontSize)) / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void pressImage(String pressImg, String srcImageFile, String destImageFile, int x, int y,
                                        float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int wideth = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(wideth, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, wideth, height, null);
            // 水印文件
            Image src_biao = ImageIO.read(new File(pressImg));
            int wideth_biao = src_biao.getWidth(null);
            int height_biao = src_biao.getHeight(null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g.drawImage(src_biao, (wideth - wideth_biao) / 2, (height - height_biao) / 2, wideth_biao, height_biao,
                    null);
            // 水印文件结束
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }

    /**
     * 获取图片尺寸信息
     *
     * @param filePath
     *            a {@link java.lang.String} object.
     * @return [width, height]
     */
    public static int[] getSizeInfo(String filePath) throws Exception {
        File file = new File(filePath);
        return getSizeInfo(file);
    }

    /**
     * 获取图片尺寸信息
     *
     * @param url
     *            a {@link java.net.URL} object.
     * @return [width,height]
     */
    public static int[] getSizeInfo(URL url) throws Exception {
        InputStream input = null;
        try {
            input = url.openStream();
            return getSizeInfo(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * 获取图片尺寸信息
     *
     * @param file
     *            a {@link java.io.File} object.
     * @return [width,height]
     */
    public static int[] getSizeInfo(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("file " + file.getAbsolutePath() + " doesn't exist.");
        }
        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            return getSizeInfo(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * 获取图片尺寸
     *
     * @param input
     *            a {@link java.io.InputStream} object.
     * @return [width,height]
     */
    public static int[] getSizeInfo(InputStream input) throws Exception {
        try {
            BufferedImage img = ImageIO.read(input);
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            return new int[] { w, h };
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    /**
     * 重调图片尺寸
     *
     * @param srcFilePath
     *            原图路径
     * @param destFile
     *            目标文件
     * @param width
     *            新的宽度，小于1则忽略，按原图比例缩放
     * @param height
     *            新的高度，小于1则忽略，按原图比例缩放
     */
    public static void resize(String srcFilePath, String destFile, int width, int height) throws Exception {
        resize(srcFilePath, destFile, width, height, -1, -1);
    }

    /**
     * 重调图片尺寸
     *
     * @param input
     *            a {@link java.io.InputStream} object.
     * @param output
     *            a {@link java.io.OutputStream} object.
     * @param width
     *            a int.
     * @param height
     *            a int.
     */
    public static void resize(InputStream input, OutputStream output, int width, int height) throws Exception {
        resize(input, output, width, height, -1, -1);
    }

    /**
     * 重调图片尺寸
     *
     * @param input
     *            a {@link java.io.InputStream} object.
     * @param output
     *            a {@link java.io.OutputStream} object.
     * @param width
     *            a int.
     * @param height
     *            a int.
     * @param maxWidth
     *            a int.
     * @param maxHeight
     *            a int.
     */
    public static void resize(InputStream input, OutputStream output, int width, int height, int maxWidth,
                              int maxHeight) throws Exception {

        if (width < 1 && height < 1 && maxWidth < 1 && maxHeight < 1) {
            try {
                IOUtils.copy(input, output);
            } catch (IOException e) {
                throw new Exception("resize error: ", e);
            }
        }
        try {
            BufferedImage img = ImageIO.read(input);
            boolean hasNotAlpha = !img.getColorModel().hasAlpha();
            double w = img.getWidth(null);
            double h = img.getHeight(null);
            int toWidth;
            int toHeight;
            double rate = w / h;

            if (width > 0 && height > 0) {
                rate = ((double) width) / ((double) height);
                toWidth = width;
                toHeight = height;
            } else if (width > 0) {
                toWidth = width;
                toHeight = (int) (toWidth / rate);
            } else if (height > 0) {
                toHeight = height;
                toWidth = (int) (toHeight * rate);
            } else {
                toWidth = ((Number) w).intValue();
                toHeight = ((Number) h).intValue();
            }

            if (maxWidth > 0 && toWidth > maxWidth) {
                toWidth = maxWidth;
                toHeight = (int) (toWidth / rate);
            }
            if (maxHeight > 0 && toHeight > maxHeight) {
                toHeight = maxHeight;
                toWidth = (int) (toHeight * rate);
            }

            BufferedImage tag = new BufferedImage(toWidth, toHeight,
                    hasNotAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);

            // Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
            tag.getGraphics().drawImage(img.getScaledInstance(toWidth, toHeight, Image.SCALE_SMOOTH), 0, 0, null);
            ImageIO.write(tag, hasNotAlpha ? "jpg" : "png", output);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

    }

    /**
     * 重调图片尺寸
     *
     * @param srcFile
     *            原图路径
     * @param destFile
     *            目标文件
     * @param width
     *            新的宽度，小于1则忽略，按原图比例缩放
     * @param height
     *            新的高度，小于1则忽略，按原图比例缩放
     * @param maxWidth
     *            最大宽度，限制目标图片宽度，小于1则忽略此设置
     * @param maxHeight
     *            最大高度，限制目标图片高度，小于1则忽略此设置
     */
    public static void resize(String srcFile, String destFile, int width, int height, int maxWidth, int maxHeight)
            throws Exception {
        resize(new File(srcFile), new File(destFile), width, height, maxWidth, maxHeight);
    }

    /**
     * 重调图片尺寸
     *
     * @param srcFile
     *            原图路径
     * @param destFile
     *            目标文件
     * @param width
     *            新的宽度，小于1则忽略，按原图比例缩放
     * @param height
     *            新的高度，小于1则忽略，按原图比例缩放
     */
    public static void resize(File srcFile, File destFile, int width, int height) throws Exception {
        resize(srcFile, destFile, width, height, -1, -1);
    }

    /**
     * 重调图片尺寸
     *
     * @param srcFile
     *            原图路径
     * @param destFile
     *            目标文件
     * @param width
     *            新的宽度，小于1则忽略，按原图比例缩放
     * @param height
     *            新的高度，小于1则忽略，按原图比例缩放
     * @param maxWidth
     *            最大宽度，限制目标图片宽度，小于1则忽略此设置
     * @param maxHeight
     *            最大高度，限制目标图片高度，小于1则忽略此设置
     */
    public static void resize(File srcFile, File destFile, int width, int height, int maxWidth, int maxHeight)
            throws Exception {
        if (destFile.exists()) {
            destFile.delete();
        } else {
            mkdirs(destFile.getParent());
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new BufferedInputStream(new FileInputStream(srcFile));
            output = new FileOutputStream(destFile);
            resize(input, output, width, height, maxWidth, maxHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 裁剪图片
     *
     * @param source
     *            a {@link java.lang.String} object.
     * @param target
     *            a {@link java.lang.String} object.
     * @param x
     *            a int.
     * @param y
     *            a int.
     * @param w
     *            a int.
     * @param h
     *            a int.
     */
    public static void crop(String source, String target, int x, int y, int w, int h) throws Exception {
        crop(new File(source), new File(target), x, y, w, h);
    }

    /**
     * 裁剪图片
     *
     * @param source
     *            a {@link java.io.File} object.
     * @param target
     *            a {@link java.io.File} object.
     * @param x
     *            a int.
     * @param y
     *            a int.
     * @param w
     *            a int.
     * @param h
     *            a int.
     */
    public static void crop(File source, File target, int x, int y, int w, int h) throws Exception {
        OutputStream output = null;
        InputStream input = null;
        String ext = FilenameUtils.getExtension(target.getName());
        try {
            input = new BufferedInputStream(new FileInputStream(source));
            if (target.exists()) {
                target.delete();
            } else {
                mkdirs(target.getParent());
            }
            output = new BufferedOutputStream(new FileOutputStream(target));
        } catch (IOException e) {
            throw new Exception(e);
        }
        crop(input, output, x, y, w, h, StringUtils.equalsIgnoreCase("png", ext));
    }

    /**
     * 裁剪图片
     *
     * @param x
     *            a int.
     * @param y
     *            a int.
     * @param w
     *            a int.
     * @param h
     *            a int.
     * @param input
     *            a {@link java.io.InputStream} object.
     * @param output
     *            a {@link java.io.OutputStream} object.
     * @param isPNG
     *            a boolean.
     */
    public static void crop(InputStream input, OutputStream output, int x, int y, int w, int h, boolean isPNG)
            throws Exception {
        try {
            BufferedImage srcImg = ImageIO.read(input);
            int tmpWidth = srcImg.getWidth();
            int tmpHeight = srcImg.getHeight();
            int xx = Math.min(tmpWidth - 1, x);
            int yy = Math.min(tmpHeight - 1, y);

            int ww = w;
            if (xx + w > tmpWidth) {
                ww = Math.max(1, tmpWidth - xx);
            }
            int hh = h;
            if (yy + h > tmpHeight) {
                hh = Math.max(1, tmpHeight - yy);
            }

            BufferedImage dest = srcImg.getSubimage(xx, yy, ww, hh);

            BufferedImage tag = new BufferedImage(w, h,
                    isPNG ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

            tag.getGraphics().drawImage(dest, 0, 0, null);
            ImageIO.write(tag, isPNG ? "png" : "jpg", output);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 压缩图片,PNG图片按JPG处理
     *
     * @param input
     *            a {@link java.io.InputStream} object.
     * @param output
     *            a {@link java.io.OutputStream} object.
     * @param quality
     *            图片质量0-1之间
     */
    public static final void optimize(InputStream input, OutputStream output, float quality) throws Exception {

        BufferedImage image;
        ImageOutputStream ios = null;
        ImageWriter writer = null;
        try {
            image = ImageIO.read(input);

            // get all image writers for JPG format
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");

            if (!writers.hasNext())
                throw new IllegalStateException("No writers found");

            writer = (ImageWriter) writers.next();
            ios = ImageIO.createImageOutputStream(output);

            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            // optimize to a given quality
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            // appends a complete image stream containing a single image and
            // associated stream and image metadata and thumbnails to the output
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if (ios != null) {
                try {
                    ios.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new Exception(e);
                }
            }
            writer.dispose();
        }
    }

    /**
     * 压缩图片
     *
     * @param source
     *            a {@link java.lang.String} object.
     * @param target
     *            a {@link java.lang.String} object.
     * @param quality
     *            a float.
     */
    public static final void optimize(String source, String target, float quality) throws Exception {
        File fromFile = new File(source);
        File toFile = new File(target);
        optimize(fromFile, toFile, quality);
    }

    /**
     * 压缩图片
     *
     * @param source
     *            a {@link java.io.File} object.
     * @param target
     *            a {@link java.io.File} object.
     * @param quality
     *            图片质量0-1之间
     */
    public static final void optimize(File source, File target, float quality) throws Exception {
        if (target.exists()) {
            target.delete();
        } else {
            mkdirs(target.getParent());
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(new FileInputStream(source));
            os = new BufferedOutputStream(new FileOutputStream(target));
            optimize(is, os, quality);
        } catch (FileNotFoundException e) {
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * 制作圆角
     *
     * @param srcFile
     *            原文件
     * @param destFile
     *            目标文件
     * @param cornerRadius
     *            角度
     */
    public static void makeRoundedCorner(File srcFile, File destFile, int cornerRadius) throws Exception {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new BufferedInputStream(new FileInputStream(srcFile));
            mkdirs(destFile.getParentFile().getAbsolutePath());
            out = new BufferedOutputStream(new FileOutputStream(destFile));
            makeRoundedCorner(in, out, cornerRadius);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }

    }

    /**
     * 制作圆角
     *
     * @param srcFile
     *            原文件
     * @param destFile
     *            目标文件
     * @param cornerRadius
     *            角度
     */
    public static void makeRoundedCorner(String srcFile, String destFile, int cornerRadius) throws Exception {
        makeRoundedCorner(new File(srcFile), new File(destFile), cornerRadius);
    }

    /**
     * 制作圆角
     *
     * @param inputStream
     *            原图输入流
     * @param outputStream
     *            目标输出流
     * @param radius
     *            角度
     */
    public static void makeRoundedCorner(final InputStream inputStream, final OutputStream outputStream,
                                         final int radius) throws Exception {
        BufferedImage sourceImage = null;
        BufferedImage targetImage = null;
        try {
            sourceImage = ImageIO.read(inputStream);
            int w = sourceImage.getWidth();
            int h = sourceImage.getHeight();
            System.out.println(w);

            int cornerRadius = radius < 1 ? w / 4 : radius;

            targetImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = targetImage.createGraphics();

            g2.setComposite(AlphaComposite.Src);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

            g2.setComposite(AlphaComposite.SrcAtop);
            g2.drawImage(sourceImage, 0, 0, null);
            g2.dispose();
            ImageIO.write(targetImage, "png", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    /**
     * TODO 有点儿问题，,最后一级创建的是文件，而不是目录，待修改
     * @param path
     */
    private static void mkdirs(String path) {

        try {

            FileUtils.forceMkdirParent(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }