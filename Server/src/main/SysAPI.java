/**
 * 本模块提供的API包括：
 * 1. Version（无函数）
 * 2. DBinit（无函数）
 */
package main;
import org.apache.log4j.*;
import base.*;

public class SysAPI {
	DBDriver dbd = new DBDriver();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	Logger logger = Logger.getLogger(SysAPI.class.getName());
	
	/**[Function] 				API解释模块，根据API检查必要的参数和请求数据的完整性，调用对应API实现模块
	 * @param API			API字符串
	 * @return [String]		Json格式字符串，返回API执行的结果
	 */
	public String DoAPI(String API){						
		PropertyConfigurator.configure(logconf);		
		String message="";
		logger.info("API: "+API);
//		获取当前操作用户
		String code="412";
		String backvalue="";
//		处理API
		switch(API){
		case "Version":
			return "{\"Version_core\": \"1.0.0\","
					+ "\"Author\":\"李昊\","
					+ "\"Release_time\":\"2016-11-30\"}";
		case "DBinit":
			try {
				dbd.DBinit();
				message="数据库初始化完成。";
			} catch (Throwable e) {
				message="数据库初始化失败，原因："+e.toString();
			}
			return message;
		default:
			logger.error("无效API: "+API);
			backvalue="400,无效API!";
		}
		code=backvalue.substring(0,backvalue.indexOf(","));
		message=backvalue.substring(backvalue.indexOf(",")+1);
		backvalue="{\"code\":"+code+",\"message\":\""+message+"\"}";
		return backvalue;
	}
}