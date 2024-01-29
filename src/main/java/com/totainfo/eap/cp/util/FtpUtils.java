package com.totainfo.eap.cp.util;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;

public class FtpUtils {
	private static String LOCAL_CHARSET = "GBK";

	// FTP协议里面，规定文件名编码为iso-8859-1
	private static String SERVER_CHARSET = "ISO-8859-1";
	/**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, int ftpPort, String ftpUserName, String ftpPassword) {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				LogUtils.error("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            }
        } catch (SocketException e) {
			LogUtils.info("[FTP的IP地址可能错误，请正确配置]");
        } catch (IOException e) {
			LogUtils.info("[FTP的端口错误,请正确配置]");
        }
        return ftpClient;
    }

	/**
	* 上传文件
	* @param directory 上传的目录
	* @param file 要上传的文件
	* @param ftp
	* @param ftpHost 要上传的文件
	* @param ftpPort 要上传的文件
	* @param ftpUserName 要上传的文件
	* @param ftpPassword 要上传的文件
	*/
	public static boolean upload(String directory, File file, FTPClient ftp, String ftpHost, int ftpPort, String ftpUserName,String ftpPassword ){
		try {
			int reply = ftp.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(reply)) {
				disconnect(ftp);
				LogUtils.info("FTP连接失效，重新连接。。。");
				ftp = getFTPClient(ftpHost, ftpPort, ftpUserName, ftpPassword);
	        }
	        LOCAL_CHARSET = "UTF-8"; // 中文支持LOCAL_CHARSET = "UTF-8";
	        ftp.setControlEncoding(LOCAL_CHARSET); // 中文支持
	        ftp.enterLocalPassiveMode();
			ftp.setFileType(ftp.BINARY_FILE_TYPE);
			ftp.setFileTransferMode(ftp.STREAM_TRANSFER_MODE);
			if(directory != null && !"".equals(directory.trim())){
				ftp.changeWorkingDirectory(directory);
			}
			if(!file.exists()) {
				return false;
			}
			InputStream inputStram = new FileInputStream(file);
			ftp.storeFile(new String(file.getName().getBytes(LOCAL_CHARSET),SERVER_CHARSET), inputStram);
			inputStram.close();
	        return true;
		} catch (IOException e) {
			LogUtils.error("文件上传失败，原因", e); ;
		}
		return false;
	}

	    /**
	    * 断开sftp服务器连接
	    *
	    * @throws Exception
	    */
	   public static void disconnect(FTPClient ftp){
	      if (ftp != null){
	         if (ftp.isConnected()){
	        	 try {
					ftp.disconnect();
				} catch (IOException e) {
					 LogUtils.error("FTP连接关闭失败，原因", e); ;
				}
	         }
	      }
	   }



    /**
     * Description: 向FTP服务器上传文件
     * @param ftpHost FTP服务器hostname
     * @param ftpUserName 账号
     * @param ftpPassword 密码
     * @param ftpPort 端口
     * @param ftpPath  FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName ftp文件名称
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String ftpHost, String ftpUserName,
                                     String ftpPassword, int ftpPort, String ftpPath,
                                     String fileName) {
        boolean success = false;
        FTPClient ftpClient = null;
            int reply;
		try {
			ftpClient = getFTPClient(ftpHost, ftpPort, ftpUserName, ftpPassword);
			reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				return false;
			}
		} catch (IOException e) {
			LogUtils.error("FTP连接异常，原因", e); ;
	    }
	    try{
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
			ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(ftpClient.STREAM_TRANSFER_MODE);
            ftpClient.changeWorkingDirectory(ftpPath);
            File file = new File(fileName);
            if(!file.exists()) {
            	return false;
            }
            InputStream inputStream = new FileInputStream (file);

            ftpClient.storeFile(file.getName(), inputStream);

            inputStream.close();
            ftpClient.logout();
        }catch (IOException e) {
			e.printStackTrace();
		}
		return success;
    }
}
