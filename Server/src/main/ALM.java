package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mercury.qualitycenter.otaclient.ClassFactory;
import com.mercury.qualitycenter.otaclient.IBug;
import com.mercury.qualitycenter.otaclient.IBugFactory;
import com.mercury.qualitycenter.otaclient.IList;
import com.mercury.qualitycenter.otaclient.ITDConnection;
import com.mercury.qualitycenter.otaclient.ITDFilter2;

import com4j.Com4jObject;
import base.DBDriver;
import base.User;

public class ALM {
	DBDriver dbd = new DBDriver();
	User user=new User();	
	
	ITDConnection connection = ClassFactory.createTDConnection();
	IBugFactory bugFactory;
	ITDFilter2 filter;
	IList bugList;

//	配置日志属性文件位置
	String confpath=System.getProperty("user.dir").replace("\\bin", "");
	String Sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";	
	Logger logger = Logger.getLogger(WeekReport.class.getName());
	
//	定义时间格式
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy/MM/dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf_short = new SimpleDateFormat("yyMMddHHmmss");
	
//	定义ALM中Bug的属性map
	HashMap<String,String> Bug_map=new HashMap<String,String>();
	
	/**[Function] 				API解释模块，根据API检查必要的参数和请求数据的完整性，调用对应API实现模块
	 * @param API			API字符串
	 * @param Param		API请求中URL携带的参数表
	 * @param body		API请求携带的body数据，Json格式字符串
	 * @param token		校验字，API请求携带的header参数，用来保证报文的可靠性和安全性
	 * @return [String]		Json格式字符串，返回API执行的结果
	 */
	public String DoAPI(String API,Map<String, String[]> Param,String body){						
		PropertyConfigurator.configure(logconf);		
		String message="";
		logger.info("API: "+API+" "+" [Body]"+body);
//		获取当前操作用户
		String backvalue="412,http 请求的参数缺失或无效";
		
//		Bug属性初始化
		Bug_map.put("id", "BG_BUG_ID");
		Bug_map.put("summary", "BG_SUMMARY");
		Bug_map.put("state", "BG_STATUS");
		Bug_map.put("severity", "BG_SEVERITY");
		Bug_map.put("tester", "BG_DETECTED_BY");
		Bug_map.put("assignto", "BG_RESPONSIBLE");
		Bug_map.put("defect_time", "BG_DETECTION_DATE");
		Bug_map.put("module", "BG_USER_01");
		Bug_map.put("version", "BG_USER_02");
		
		String Bugfilter=checkpara(Param,"filter");
		try {
			switch(API){		
			case "BugList":   	
				logger.info("从ALM中获取Bug列表...");				
				String domain=checkpara(Param,"domain");
				String proj=checkpara(Param,"proj");
				String page_count=checkpara(Param,"page_count");
				String page_num=checkpara(Param,"page_num");
				if(!domain.equals("") && !proj.equals("")) return ListBug(domain,proj,Bugfilter,page_count,page_num);	
				break;
			case "Fresh": 
				String domains=checkpara(Param,"domain");
				String projs=checkpara(Param,"proj");
				if(!domains.equals("") && !projs.equals("")) {
					readALM_bug(domains,projs);
					backvalue="200,ok";
				}				
				break;	
			case "DownloadBGL":
				logger.info("下载Bug列表...");
				return download_buglist(Bugfilter);			
			default:
				logger.error("无效API: "+API);
				backvalue="400,无效API!";
			}
		}catch (Throwable e) {
			backvalue=e.getMessage();
			int firtag=backvalue.lastIndexOf("[info]");
			if(firtag>-1) backvalue=backvalue.substring(firtag+6);
			else backvalue="500,"+backvalue;
			logger.error(backvalue,e);			
		}	
		String code=backvalue.substring(0,backvalue.indexOf(","));
		message=backvalue.substring(backvalue.indexOf(",")+1);
		backvalue="{\"code\":"+code+",\"message\":\""+message+"\"}";
		return backvalue;
	}
	
	/**
	 * [说明] 按过滤条件查询Bug并通过JSON格式返回
	 * @param filter	过滤条件，需要符合MySQL的语法规则
	 * @param page_count	每页要显示的条目数
	 * @param page_num	指定要显示的页码
	 * @return	 JSON格式的Buglist，包含字段id,summary,assignto,state,severity,version,tester,defect_time,module
	 * @throws Exception
	 */
	String ListBug(String domain,String proj,String Bug_filter,String page_count,String page_num) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			检查缓存器里是否有Bug，没有的话就从新读取一次
			String cols="id,summary,assignto,state,severity,version,tester,defect_time,module";
			String[][] Bugs=dbd.readDB("cache_buglist", "*", "domain='"+domain+"' and proj='"+proj+"'");
			if(Bugs[0][0].equals(""))readALM_bug(domain,proj);
			
//			使用过滤器读取数据库
			JSONObject Bug=new JSONObject();
			JSONArray BugList=new JSONArray();
			String ft="id>0";
			if(!Bug_filter.equals("")) {
				ft=Bug_filter;
				if(ft.indexOf("=")==-1)ft=ft.replace("*", "%");
			}
			Bugs=dbd.readDB("cache_buglist", cols, ft);
			if(Bugs[0][0].equals(""))throw new Exception("[info]404,"+domain+"-"+proj+"下没有找到Bug数据。");
			int num=Bugs.length;
			int last_num=num;
			int first_num=0;
			Bug.put("page_count", page_count);
			Bug.put("page_num", page_num);
			if(num>0) {
				if(!page_count.equals("")) {
					int nn=Integer.parseInt(page_count);
//					如果每页记录数大于实际记录数，则全部返回，否则分页显示
					if(num>nn) {
//						如果页码为空，则默认返回第一页
						if(!page_num.equals("")) {
							int mm=Integer.parseInt(page_num);
//							如果页码*单页记录数大于实际记录则返回空，否则从新计算起始页
							if(mm<1 || (mm-1)*nn>=num) {
								Bug.put("Buglist", BugList);
								return Bug.toString();
							}
							else {
								first_num=(mm-1)*nn;						
								if(num>mm*nn)last_num=first_num+nn;
							}
						}
						else	last_num=first_num+nn;
					}
				}
				int j=0;
				for(int  i=first_num;i<last_num;i++) {
					JSONObject jsb=new JSONObject();					
					jsb.put("id", Bugs[i][0]);
					jsb.put("summary", Bugs[i][1]);
					jsb.put("assignto", Bugs[i][2]);
					jsb.put("state",Bugs[i][3]);
					jsb.put("severity", Bugs[i][4]);
					jsb.put("version", Bugs[i][5]);
					jsb.put("tester", Bugs[i][6]);
					String tt=sdf_ymd.format(sdf_full.parse(Bugs[i][7]));					
					jsb.put("defect_time",tt);
					jsb.put("module", Bugs[i][8]);	
					BugList.put(j,jsb);
					j++;
				}
			}					
			Bug.put("Bug_num", num);			
			Bug.put("Buglist", BugList);
			Bug.put("code",200);
			return Bug.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e.getMessage());
		}
		
	}
	
	/**
	 * 说明：
	 * @param BugID
	 * @return
	 * @throws Exception
	 */
	String GetBug(String BugID) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
			JSONObject Bug=new JSONObject();
			Bug.put("id", "");
			Bug.put("summary", "");
			Bug.put("assignto", "");
			Bug.put("state", "");
			Bug.put("severity", "");
			Bug.put("version", "");
			Bug.put("tester", "");
			Bug.put("testtime", "");
			Bug.put("module", "");
			return Bug.toString();
		}catch(Throwable e) {
			throw new Exception(e.getMessage());
		}
		
	}
	
	/**
	 * [说明]	从ALM中读取指定域和项目的所有Bug
	 * @param domain	要指定的域，等价于产品线
	 * @param project		要指定的项目
	 * @throws Exception
	 */
	void readALM_bug(String domain,String project) throws Exception{
        IList bugList;
        IBug Bugg;       
        String key="BG_BUG_ID";
        String value=">0";
        try {
        	ConnectALM(domain, project, "lihao", "313028");
        	filter.clear();
            filter.filter(key, value);
            bugList = filter.newList();
            int count=bugList.count();
            if(count == 0) throw new Exception("[info]404,没有找到符合条件的Bug");     
//          清除数据库缓存
            dbd.DelSQl("cache_buglist", 0, 1, 1);
//          读取Bug并写入数据库
            String[] colname= {"id","domain","proj","assignto","tester","summary","state","severity","version","defect_time","module"};
            String[] record =new String[colname.length];
            record[1]=domain;
            record[2]=project;
            SimpleDateFormat sdf1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
            for(int i=1;i<=count;i++){
            	Com4jObject comObj = (Com4jObject) bugList.item(i);
             	Bugg = comObj.queryInterface(IBug.class);
             	record[0]=Bugg.id().toString();
             	record[3]=Bugg.assignedTo();
             	record[4]=Bugg.detectedBy();
             	record[5]=Bugg.summary();
             	record[6]=Bugg.status();
             	record[7]=Bugg.field("BG_SEVERITY").toString();
             	record[8]=Bugg.field("BG_USER_02").toString();        
             	record[9]=sdf_ymd.format(sdf1.parse(Bugg.field("BG_DETECTION_DATE").toString()));
             	record[10]=Bugg.field("BG_USER_01").toString();
             	dbd.AppendSQl("cache_buglist", colname, record, 1, 1);
            }
            connection.disconnectProject();
			connection.releaseConnection();
        }catch(Throwable e) {
        	logger.error(e.getMessage(),e);
        	throw new Exception(e.getMessage());
        }       
	}
	/**[Function] 				生成Bug文件，并返回下载文件的地址
	 * @author filter		Bug的过滤条件
	 * @return 		返回文件下载地址
	 */
	String download_buglist(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			String LRP_path=confpath+"\\webapps\\ASWS\\datareport\\buglist.csv";	
			File tempfile = new File(LRP_path);
			if (tempfile.exists())tempfile.delete();
			BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (tempfile), "GB2312"));
			bw.write("ID,摘要,分配给,状态,严重程度,版本,测试者,测试日期,功能模块");
			bw.newLine();
			
			if(filter.equals(""))filter="id>0";	
			else if(filter.indexOf("=")==-1)filter=filter.replace("*", "%");	
			String[][] dlg=dbd.readDB("cache_buglist", "id,summary,assignto,state,severity,version,tester,defect_time,module", filter);
			if(!dlg[0][0].equals("")){
				String linr="";
				for(int i=0;i<dlg.length;i++) {
					dlg[i][7]=sdf_ymd.format(sdf_full.parse(dlg[i][7]));
					linr=dlg[i][0]+","+dlg[i][1]+","+dlg[i][2]+","+dlg[i][3]+","+dlg[i][4]+","+dlg[i][5]+","+dlg[i][6]+","+dlg[i][7]+","+dlg[i][8];
					bw.write(linr);
					bw.newLine();
				}
			}		
			bw.flush();
			bw.close();
			return "{\"code\":200,\"DownloadFile_url\":\"buglist.csv\"}";
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}				
	}
	void ConnectALM(String domain,String project,String user,String pwd) throws Exception {	
		PropertyConfigurator.configure(logconf);	
		logger.info("连接ALM");
		try {
			if(!connection.connected()) {
				connection.initConnectionEx("http://192.168.56.12:8080/qcbin");
				connection.connectProjectEx(domain, project, user, pwd);
			}
			bugFactory = (IBugFactory) connection.bugFactory().queryInterface(IBugFactory.class);
		    filter = bugFactory.filter().queryInterface(ITDFilter2.class);	
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e.getMessage());
		}		
	}
	
	/**[Function] 				获取http请求报文中的参数值
	 * @author para		请求报文中的参数序列
	 * @author key			预期的参数名
	 * @return [String]		返回参数结果，如果请求的参数序列为空，或者没有要查询的参数，返回“”，否则返回查询到的参数值
	 */
	String checkpara(Map<String,String[]> para,String key){
		PropertyConfigurator.configure(logconf);
		String ba="";		
		if(para.size()>0){
			try{
				String[] val=para.get(key);
				if(null!=val)ba=val[0];
			}catch(NullPointerException e){
				logger.error(e.getMessage());
			}
		}	
		return ba;
	}
}
