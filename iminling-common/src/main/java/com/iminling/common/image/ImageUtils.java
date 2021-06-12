package com.iminling.common.image;

import com.iminling.common.crypto.Base64Utils;
import com.iminling.common.file.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;

public class ImageUtils {
    
    private static Logger logger = LoggerFactory.getLogger(ImageUtils.class.getName());

    private static String URL_PATH = "/web/images";
    private static String URL_PATH_THUMB = URL_PATH + "/thumbnail";

    /**
     * 生成文件名称-无后缀
     * @return {@link String}
     */
    public static String generateFileName() {
        return generateFileName(null);
    }

    /**
     * 生成特定扩展的文件名称
     * @param extension 后缀名称
     * @return {@link String}
     */
    public static String generateFileName(String extension) {
        Calendar now = Calendar.getInstance();
        String pathFileName = "/" +
                now.get(Calendar.YEAR) + "/" +
                (now.get(Calendar.MONTH) + 1) + "/" +
                now.get(Calendar.DAY_OF_MONTH) + "/"
                + UUID.randomUUID();
        if (extension == null) {
            return pathFileName;
        } else {
            return pathFileName + "." + extension;
        }
    }

    /**
     * 根据上传文件的后缀名生成新的文件名称
     * @param fileName 文件名称
     * @return {@link String}
     */
    public static String getFileName(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        return generateFileName(extension);

    }

    /**
     * 安照一定的长宽或者比例进行图片压缩
     * @param inputStream 文件流
     * @param width 宽
     * @param height 高
     * @param rate 比例
     * @return {@link String}
     */
    public static String uploadImage(InputStream inputStream, int width,
                                     int height, Float rate) {
        try {
            BufferedImage src = ImageIO.read(inputStream);
            // 如果rate不为空说明是按比例压缩
            if (rate != null && rate > 0) {
                // 获取文件高度和宽度
                int results[] = { 0, 0 };
                results[0] = src.getWidth(null); // 得到源图宽
                results[1] = src.getHeight(null); // 得到源图高
                if (results[0] == 0 || results[1] == 0) {
                    return null;
                } else {
                    width = (int) (results[0] * rate);
                    height = (int) (results[1] * rate);
                }
            }
            // 开始读取文件并进行压缩
            BufferedImage tag = new BufferedImage(width,
                    height, BufferedImage.TYPE_INT_RGB);
            tag.getGraphics().drawImage(
                    src.getScaledInstance(width, height,
                            Image.SCALE_SMOOTH), 0, 0, null);
            return uploadImage(tag, URL_PATH_THUMB);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 安照一定的长宽或者比例进行图片压缩
     * @param base64EncodeFileData base64编码过的图片信息
     * @param width 宽
     * @param height 高
     * @param rate 比例
     * @return {@link String}
     */
    public static String uploadImage(String base64EncodeFileData, int width,
                                     int height, Float rate) {
        byte[] imageByte = Base64Utils.base64FileToByte(base64EncodeFileData);
        InputStream inputStream = new ByteArrayInputStream(imageByte);
        return uploadImage(inputStream,width,height,rate);
    }

    /**
     * 上传通过base64编码后文件数据
     * @param base64EncodeFileData base64字符串
     * @return {@link String}
     */
    public static String uploadImage(String base64EncodeFileData) {
        byte[] imageByte = Base64Utils.base64FileToByte(base64EncodeFileData);
        return uploadImage(imageByte);
    }

    /**
     * MultipartFile myFile = uploadVO.getFile();
     * ImageUtils.uploadImage(myFile.getBytes());
     * 使用spring mvc自带的MultipartFile
     * @param imageByte 图片字节数组
     * @return {@link String}
     */
    public static String uploadImage(byte[] imageByte) {
        try {
            if (imageByte != null) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageByte));
                return uploadImage(image, URL_PATH);
            } else {
                logger.error("************************************************");
                logger.error("*** IMAGE IN REQUEST IS NOT IN BASE64 FORMAT ***");
                logger.error("************************************************");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 上传文件.
     * @param image 图片buffer
     * @param resUrlPath url
     * @return {@link String}
     */
    public static String uploadImage(BufferedImage image, String resUrlPath) {
        /*
        需要理解以下概念(需要区分物理存储路径与URL访问路径的区别):
        对于一个图片资源,其URL是:http://res.xxxx.com/web/images/default.jpg
        1. resUrlPath: URL访问的路径, 上述资源的resUrlPath是： /web/images/
        2. realRootPath: 物理存储路径，上述资源的realRootPath是: /data/res/web/images/
        3. uploadFileName：文件名: 上述资源的uploadFileName是：default.jpg
         */
        String osName = System.getProperties().getProperty("os.name");
        // rootPath一定要用绝对路径,否则在服务器创建文件的时候可能导致报错
        String rootPath = "/data/res"; //物理存储的根路径,根路径后面的路径与URL的路径是一致的
        if("Mac OS X".equals(osName)){
            rootPath = "/Users/konghang/data/res";
        }
        String realRootPath = rootPath + resUrlPath; //物理存储路径

        String extension;
        int imageType = image.getType();
        if (imageType == BufferedImage.TYPE_4BYTE_ABGR || imageType == BufferedImage.TYPE_INT_ARGB) {
            extension = "png"; //带alpha通道的数据，统一转换成png
        } else {
            extension = "jpg";  //所有图片统一转换成jpg(性价比最好)
        }

        String uploadFileName = generateFileName(extension);  //文件名,比如,default.jpg
        String resFileName = resUrlPath + uploadFileName;     //资源URL访问路径,比如: /web/images/default.jpg
        String FullFileName = realRootPath + uploadFileName;  //文件存储的物理路径,比如: /data/res/web/images/default.jpg

        logger.trace("FullFileName:" + FullFileName);
        try {
            File file = FileUtil.createFile(FullFileName);
            ImageIO.write(image, extension, file);
            FileUtil.setFilePermission(FullFileName); //设置文件读写权限
            return resFileName;
        } catch (Exception e) {
            logger.error("transfer File Error:" + e.getStackTrace());
            return null;
        }
    }

    /**
     * 通过远程图片url读取图片为bufferedImage
     * @param destUrl 目标地址
     * @return {@link BufferedImage}
     * @throws Exception 异常
     */
    public static BufferedImage loadRemoteImage(String destUrl) throws Exception {
        byte[] imageByte = loadRemoteBytes(destUrl);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageByte));
        return image;
    }

    /**
     * 加载远程图片
     * @param destUrl 图片地址
     * @return 字节数组
     * @throws Exception 异常
     */
    public static byte[] loadRemoteBytes(String destUrl) throws Exception {
        ///new一个URL对象
        URL url = new URL(destUrl);
        //打开链接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为5秒
        conn.setConnectTimeout(10 * 1000);
        //通过输入流获取图片数据
        InputStream inStream = conn.getInputStream();
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        return readInputStream(inStream);
    }

    /**
     * 读取inputstream
     * @param inStream stream
     * @return 字节数组
     * @throws Exception 异常
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    /**
     * 上传文件
     * @param multipartFile springMvc文件类型
     * @param realRootPath 上传路径
     * @return 路径
     */
    public static String uploadFile(MultipartFile multipartFile, String realRootPath) {
        String fileName = multipartFile.getOriginalFilename();
        String uploadFileName = getFileName(fileName);
        logger.trace("uploadFileName:" + uploadFileName + ",realRootPath:" + realRootPath);
        try {
            File file = FileUtil.createFile(realRootPath + uploadFileName);
            multipartFile.transferTo(file);
            return URL_PATH + uploadFileName;
        } catch (IOException e) {
            logger.error("transfer File Error:" + e.getStackTrace());
            return null;
        }
    }

    /**
     * 保存byte[]到文件
     * @param byteImages 文件字节数组
     * @param fileName 文件名称
     * @return 是否保存成功
     */
    public static boolean saveToFile(byte[] byteImages, String fileName) {
        try {
            logger.trace("save to file:" + fileName);
            File file = FileUtil.createFile(fileName);
            FileOutputStream fop = new FileOutputStream(file);
            fop.write(byteImages);
            fop.flush();
            fop.close();
            FileUtil.setFilePermission(fileName);
            return true;
        } catch (Exception e) {
            logger.error("save file fail:" + fileName + "," + e.getMessage());
            return false;
        }
    }


    /**
     * 获取文件大小
     * @param base64EncodeFileData base64文件字符串
     * @return 文件大小
     */
    public static Integer getImageSize(String base64EncodeFileData) {
        try {
            byte[] imageByte = Base64Utils.base64FileToByte(base64EncodeFileData);
            return imageByte.length/1024;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
