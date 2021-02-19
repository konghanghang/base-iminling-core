package com.iminling.common.file;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class FileUtil {

    public static String getFilename(String path) {
        return FilenameUtils.getName(path);
    }

    //创建文件
    public static File createFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);

        //获得需要创建的目录递归目录,方便给每级目录设置权限
        Path parentPath = path.getParent();
        Set<String> pathSet = new HashSet<String>();
        while (!parentPath.toFile().exists()) {
            pathSet.add(parentPath.toFile().toString());
            parentPath = parentPath.getParent();
        }

        if (!path.getParent().toFile().exists()) {
            Files.createDirectories(path.getParent());
            //给每级目录都设置相同的权限
            for (String pathString : pathSet) {
                setFilePermission(pathString);
            }
        }
        File file = Files.createFile(path).toFile();
        setFilePermission(fileName);
        return file;

    }

    //获得默认的权限
    public static Set<PosixFilePermission> getRecommendPerm() {
        //using PosixFilePermission to set file permissions 777
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();

        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        //add others permissions
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        return perms;

    }

    //给文件设置权限
    public static boolean setFilePermission(String fileName) {
        try {
            String osName = System.getProperties().getProperty("os.name");
            if (osName.indexOf("Windows") != -1) {
                //Windows系統不支持使用Posix方法设置权限
                System.out.println("Windows OS System, Can not set Permission!");
                return true;
            }
            Set<PosixFilePermission> perms = getRecommendPerm();
            Files.setPosixFilePermissions(Paths.get(fileName), perms);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
