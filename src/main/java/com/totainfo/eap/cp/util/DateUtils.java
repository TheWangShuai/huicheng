package com.totainfo.eap.cp.util;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	private static String timestampFormattern = "yyyy-MM-dd HH:mm:ss.SSS";
	private static String dateFormattern = "yyyy-MM-dd";
	private static String timeFormattern = "yyyy-MM-dd HH:mm:ss";


	public static Timestamp getTimestamp(){
		LocalDateTime localDateTime = LocalDateTime.now();
		return Timestamp.valueOf(localDateTime);
	}

	public static String getClock(){
		LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		return localDateTime.format(dateTimeFormatter);
	}

	public static String dataTimeFormat(LocalDateTime localDateTime){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(timestampFormattern);
		return localDateTime.format(dateTimeFormatter);
	}

	public static LocalDateTime str2LocalDateTime(String dateTimeStr){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(timestampFormattern);
		return LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
	}

	public static String timestampFormat(Timestamp timestamp){
		SimpleDateFormat df = new SimpleDateFormat(timestampFormattern);
		return df.format(timestamp);
	}

	public static String getCurrentDateStr() {
		LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern(timestampFormattern);
		return localDateTime.format(format);
	}


	public static long string2Long(String dateString) {
		Timestamp timestamp=Timestamp.valueOf(dateString);
		return timestamp.getTime();
	}

	public static String long2String(long dateString) {
		SimpleDateFormat format = new SimpleDateFormat(timestampFormattern);
		return format.format(dateString);
	}

	public static LocalDateTime string2LocalDataTime(String dataTimeStr){
		DateTimeFormatter format = DateTimeFormatter.ofPattern(timestampFormattern);
		return LocalDateTime.parse(dataTimeStr, format);
	}

	public static Timestamp string2Timestamp(String dateString){
		if(StringUtils.isEmpty(dateString)){
			return null;
		}
        return Timestamp.valueOf(dateString);
    }

	public static Timestamp str2Timestamp(String dateString, String formattern){
		Timestamp timestamp = null;
		SimpleDateFormat format = new SimpleDateFormat(formattern);
		try {
			Date date = (Date) format.parseObject(dateString);
			timestamp = new Timestamp(date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

    public static String getcurrentTimestampStr(String dataFomat){
		Calendar now = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat(dataFomat);
		return format.format(now.getTime());
	}

	public static String getCurrentDate(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(dateFormattern);
        return format.format(now.getTime());
    }


	public static boolean isValidDate(String str) {
		boolean convertSuccess=true;// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			// 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// e.printStackTrace();
			//如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess=false;
		}
		return convertSuccess;
	}

	public static long getDiffSec(Timestamp startTime, Timestamp endTime) {
		long diffSec = 0;
		long nm = 1000;//

		long diff = endTime.getTime() - startTime.getTime();
		diffSec = diff / nm;// 计算差多少秒 //TODO Double
		return diffSec;
	}

	public static long getDiffMill(Timestamp startTime, Timestamp endTime){
		return endTime.getTime()-startTime.getTime();
	}


	public static String timestamp2Str(Timestamp timestamp){
		SimpleDateFormat format = new SimpleDateFormat(timestampFormattern);
		return format.format(timestamp.getTime());
	}

	public static LocalDateTime getZeroLocalDateTime(){
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		LocalDateTime midnight = now.withHour(0).withMinute(0).withSecond(0); // 设置小时、分钟、秒为0
		return midnight;
	}
}
