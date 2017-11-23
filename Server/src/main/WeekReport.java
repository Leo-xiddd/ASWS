/**
 * 本模块提供的API包括：
 * 1. Addwr(String usr,String WRdata) 
 * 2. Listwr(String usr,String type,String filter,String page_count,String page_num)
 * 3. Getwr(String WRname)
 * 4. Updatewr(String usr, String WRname,String WRdata) 
 * 5. Delwr(String usr,String WRname)
 * 6. ReleaseWR(String usr,String WRname)
 */
package main;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.*;

import base.*;

public class WeekReport {
	DBDriver dbd = new DBDriver();
	User user=new User();	
	Inform inf=new Inform();
//	配置日志属性文件位置
	String confpath=System.getProperty("user.dir").replace("\\bin", "");
	String Sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	
	Logger logger = Logger.getLogger(WeekReport.class.getName());
	
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf_short = new SimpleDateFormat("yyMMddHHmmss");
	
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
		String usr=checkpara(Param,"user");
		String backvalue="412,http 请求的参数缺失或无效";
		String wrname=checkpara(Param,"reportname");
		try {
			switch(API){		
			case "Add":   		
				if(!body.equals("")) {
					logger.info("创建新周报...");
					Addwr(usr,body);			//需要权限控制
					backvalue="200,ok";
				}				
				break;
			case "List":   	
				logger.info("获取周报列表...");
				String filter=checkpara(Param,"filter");
				String page_count=checkpara(Param,"page_count");
				String page_num=checkpara(Param,"page_num");
				String type=checkpara(Param,"type");
				if(!type.equals(""))	return Listwr(usr,type,filter,page_count,page_num);		
			case "Get":   		
				if(!wrname.equals("")) {
					logger.info("获取"+wrname+"...");
					return Getwr(usr,wrname);
				}
				break;
			case "Update":   						
				if(!body.equals("")&&(!wrname.equals(""))) {
					logger.info("更新"+wrname+"...");
					Updatewr(usr,wrname,body);
					backvalue="200,ok";
				}
				break;
			case "Save":   						
				if(!body.equals("")&&(!wrname.equals(""))) {
					logger.info("保存周报"+wrname+"调整的结果...");
					Savewr(usr,wrname,body);
					backvalue="200,ok";
				}
				break;
			case "Delete":   		
				if(!wrname.equals("")) {
					logger.info("删除"+wrname+"... ");
					Delwr(usr,wrname);
					backvalue="200,ok";
				}
				break;				
			case "Release":   		
				if(!wrname.equals("")) {
					logger.info("发布"+wrname+"... ");
					ReleaseWR(usr,wrname);
					backvalue="200,ok";
				}
				break;
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
	 * 函数说明：创建一个新的周报
	 * @param 	usr			发起操作请求的用户名
	 * @param 	WRdata		创建新周报的数据JSON格式
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空
	 * @throws 	Exception 	412,参数不正确
	 * @throws Exception 	500,数据库故障
	 */
	void Addwr(String usr,String WRdata) throws Exception {
		PropertyConfigurator.configure(logconf);		
//		周报表的字段包括："type","index","owner","year","week","author","creattime","complete","status","week_date"
		String[] Table_col= {"type","owner","year","week","author","creattime","wr_index","fname","complete","status","week_date"};
		String[] Json_col= {"type","owner","year","week","author","creat_time"};
		String[] record= new String[Table_col.length];
		try {
//			获取周报的属性数据
			JSONObject wrd=new JSONObject(WRdata);
			for(int i=0;i<Json_col.length;i++)record[i]=wrd.getString(Json_col[i]);
			String dept=wrd.getString("owner");
			String weekly=wrd.getString("week");
			String yearly=wrd.getString("year");
			String wr_tablename="wr_"+dept+"_"+yearly+"W"+weekly;
			String type=wrd.getString("type");
			record[Json_col.length]=wr_tablename;			
			record[Json_col.length+1]=dept+"-"+yearly+"第"+weekly+"周周报";			
			record[Json_col.length+3]="待补充";
			record[Json_col.length+4]=GetWeekdate(yearly,weekly);
			
//			只有正确的部门管理员才有权限创建部门周报
			String[] usrinfo=user.Get(usr);	
			if(wrd.getString("type").equals("dept")) {						
				if(!usrinfo[2].equals(dept)||(!usrinfo[5].equals("dept_admin")))throw new Exception("[info]401,您没有权限创建团队报告");
			}
			record[Json_col.length+2]=usrinfo[1];
			
//			判断周报是否已存在
			int a=dbd.checknum("sys_weekreport", "id", "wr_index='"+wr_tablename+"'");
			if(a>0)throw new Exception("[info]409,已存在该周报，请确认部门名称和时间是否正确。");
			
//			创建新周报并添加记录
			String[][] phase={{"id", "int(6)"},{"dept2", "VARCHAR(50)"},{"workitem_no", "VARCHAR(8)"},{"workitem", "VARCHAR(200)"},{"content", "VARCHAR(500)"},
					{"author", "VARCHAR(50)"},{"issue", "VARCHAR(500)"}};
			dbd.CreatTable(wr_tablename, phase);
			dbd.AppendSQl("sys_weekreport", Table_col, record, 1, 1);	
			
//			如果是部门周报，将向author发邮件提醒
			if(type.equals("dept")) {
				String author=wrd.getString("author");
				String Receivers=user.getmail(author);
				String Sender="weekly_report@asws.com";
				String Subject="周报提醒";
				String Texttype="text/html;charset=UTF-8";
				String Mail_Cont="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>周报提醒</title>";
				Mail_Cont=Mail_Cont+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，又到了一周一度写周报的日子，"+usrinfo[1]+"已经创建好了部门周报，"
						+ "请您尽快登录ASWS补充自己的工作进度</span><br><br><br><hr width='300px' align='left'></hr>";				
				Mail_Cont=Mail_Cont+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
						+ "ASWS 研发工作协作平台</a></span></body></html>";				
				inf.toemail(Receivers, Sender,Subject, Mail_Cont, Texttype, "");
			}
		} catch(JSONException e) {
			logger.error("周报属性数据不完整"+e.toString());
			throw new Exception("[info]412,周报属性数据不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：获取周报列表
	 * @param usr					发起操作请求的用户名
	 * @param type					请求的报告类型
	 * @param filter					用于选择周报的过滤器，由前端设定
	 * @param page_count		分页显示时，每页显示的条数
	 * @param page_num		分页显示时，显示的页码
	 * @return	 JSONArray格式字符串
	 * @throws Exception 500,数据库故障
	 */
	String Listwr(String usr,String type,String filter,String page_count,String page_num) throws Exception {
		PropertyConfigurator.configure(logconf);			
		try {					
//			设置过滤条件，每个用户只能看到自己的周报和所在部门周报，系统管理员和没有部门的人可以看所有周报
			String sfilter="type='"+type+"' order by creattime desc";	
			String[] usrinfo=user.Get(usr);		
			String own=usrinfo[0];	
			if(type.equals("dept"))own=usrinfo[2];
			if(!own.equals("") && !usrinfo[5].equals("sys_admin"))sfilter="owner='"+own+"' and "+sfilter;				
			if(!filter.equals(""))filter=filter+" and "+sfilter;
			else filter=sfilter;
				
			JSONObject wrs=new JSONObject();
			JSONArray wr_list=new JSONArray();
			String[][] temp=dbd.readDB("sys_weekreport", "*", filter);
			int num=0;
			if(!temp[0][0].equals(""))num=temp.length;
			int last_num=num;
//			周报表的字段包括："id","type","wr_index","fname","week_date","owner","year","week","author","creattime","complete","status"
			int first_num=0;
			wrs.put("page_count", page_count);
			wrs.put("page_num", page_num);
			wrs.put("total_num", num);
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
								wrs.put("wreport_list", wr_list);
								return wrs.toString();
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
					jsb.put("id", i+1);
					jsb.put("type", temp[i][4]);
					jsb.put("wr_index", temp[i][1]);
					jsb.put("fname", temp[i][2]);
					jsb.put("week_date",  temp[i][3]);
					jsb.put("owner", temp[i][5]);
					jsb.put("year", temp[i][6]);
					jsb.put("week", temp[i][7]);
					jsb.put("author", temp[i][8]);	
					
					jsb.put("creattime", sdf_full.format(sdf_full.parse(temp[i][9])));		
					jsb.put("complete", temp[i][10]);
					jsb.put("status", temp[i][11]);
					wr_list.put(j,jsb);
					j++;
				}
			}					
			wrs.put("wr_num", num);
			wrs.put("report_list", wr_list);
			wrs.put("code",200);
			return wrs.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}		
	}
	
	/**
	 * 函数说明：获取指定周报
	 * @param 	usr			发起操作请求的用户名
	 * @param 	WRname	周报名称
	 * @throws 	Exception 	404,用户名参数为空或请求的周报不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception 	500,系统错误
	 * @return		完整的JSON格式返回值
	 */
	String Getwr(String usr,String WRname) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			周报表的字段包括："id","dept2","workitem_no","workitem","content","author","issue"
			String[][] wr_index=dbd.readDB("sys_weekreport", "wr_index,author,complete", "fname='"+WRname+"'");
			if(wr_index[0][0].equals(""))throw new Exception("[info]404,["+WRname+"]不存在");
			
//			根据usr判断返回数据范围，如果usr不为空，则只返回周报中author=usr的内容，否则全部返回
			String filter="id>0";
			if(!usr.equals("")) {						
//				检查用户是否拥有author的权限：用户名必须在author列表里
				String author_list=wr_index[0][1];
				String[] usrinfo=user.Get(usr);	
				if(author_list.indexOf(usrinfo[1])==-1)throw new Exception("[info]401,您没有权限编辑该报告");
				filter="author='"+usrinfo[1]+"'";	
			}
			
//			获取周报中的二级部门列表
			String[][] wr=dbd.readDB(wr_index[0][0], "dept2",filter);
			String depts="";
			String dp="";
			for(int i=0;i<wr.length;i++) {
				dp="["+wr[i][0]+"]";
				if(depts.indexOf(dp)==-1)depts=depts+dp+",";
			}
			depts=depts.substring(0, depts.length()-1);
			depts=depts.replace("[", "");
			depts=depts.replace("]", "");
			String[] deptlist=depts.split(",");
			
			JSONObject works=new JSONObject();
			JSONArray dept_works=new JSONArray();
			String filter2="";
			for(int i=0;i<deptlist.length;i++) {
				filter2=filter+" and dept2='"+deptlist[i]+"'";
				wr=dbd.readDB(wr_index[0][0], "workitem_no,workitem,content,issue", filter2);
				JSONObject dept_work=new JSONObject();
				JSONArray work_items=new JSONArray();
				if(!wr[0][0].equals("")) {
					for(int j=0;j<wr.length;j++) {
						JSONObject item=new JSONObject();
						item.put("workitem_no", wr[j][0]);
						item.put("workitem", wr[j][1]);
						item.put("content", wr[j][2]);
						item.put("issue", wr[j][3]);
						work_items.put(j, item);
					}
				}
				dept_work.put("dept2", deptlist[i]);
				dept_work.put("wokitems", work_items);
				dept_works.put(i, dept_work);
			}
			works.put("dept_work", dept_works);
			works.put("author", wr_index[0][1]);
			works.put("complete", wr_index[0][2]);
			works.put("code", 200);
			return works.toString();
		}catch (JSONException e) {
			throw new Exception("[info]500,JSON语法错误："+e.toString());
		}
		catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	/**
	 * 函数说明：更新周报, 只有author可以打开并编辑，其他人只能看
	 * @param 	usr			发起操作请求的用户名
	 * @param 	WRname	周报名称
	 * @param 	WRdata		要更新的周报数据
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception 	500,系统错误
	 * usr,wrname,body
	 */
	void Updatewr(String usr, String WRname,String WRdata) throws Exception {
		PropertyConfigurator.configure(logconf);			
//		周报表的字段包括："id","dept2","workitem_no","workitem","content","author","issue"
		String[] tab_col= {"dept2","workitem_no","workitem","content","author","issue"};		
//		String[] json_col={"workitem_no","workitem","content"};	
		try {			
//			检查要更新的周报是否存在
			String[][] wr_meta=dbd.readDB("sys_weekreport", "wr_index,type,author,complete,id", "fname='"+WRname+"'");
			if(wr_meta[0][0].equals("")) throw new Exception("[info]404,["+WRname+"]不存在");
			
			int len=tab_col.length;
			JSONObject wrd=new JSONObject(WRdata);
			String[] record=new String[len];
			String author=wrd.getString("author");	
			String authorlist=wrd.getString("authorlist");	
			String dept2=wrd.getString("dept2");	
			String author_list=wr_meta[0][2];			
			JSONArray items=wrd.getJSONArray("workitems");
			len=len-1;
			
			record[0]=dept2;
			record[len-1]=author;
			int row=0;
						
//			1. 将原有周报内容中该用户的信息全部删除	
			String[][] idsa=dbd.readDB(wr_meta[0][0], "id", "author='"+author+"'");
			for(int i=0;i<idsa.length;i++) {	
				String[][] ids=dbd.readDB(wr_meta[0][0], "id", "author='"+author+"'");
				if(!ids[0][0].equals("")) {
					row=Integer.parseInt(ids[0][0]);
					dbd.DelSQl(wr_meta[0][0], row, 1, 1);
				}				
			}			
			
//			2. 对同部门其他人周报内容重新编号
			idsa=dbd.readDB(wr_meta[0][0], "id", "dept2='"+dept2+"'");
			if(!idsa[0][0].equals("")) {
				for(int i=0;i<idsa.length;i++) {	
					row=Integer.parseInt(idsa[i][0]);
					dbd.UpdateSQl(wr_meta[0][0], row, "workitem_no", ""+(i+1)+".");
				}
			}
			
//			3. 追加新内容
			row=dbd.checknum(wr_meta[0][0], "id", "dept2='"+dept2+"'");
			for(int i=0;i<items.length();i++) {
				JSONObject item=items.getJSONObject(i);
				record[1]=""+(row+i+1)+".";
				record[2]=item.getString("workitem");
				record[3]=item.getString("content");
				if(i==0) record[len]=item.getString("issue");
				else 	record[len]="";
				dbd.AppendSQl(wr_meta[0][0], tab_col, record, 1, 1);
			}
			
//			编辑周报的完成度，编写者和状态，全部完成为已完成
			String comp=wr_meta[0][3];
			if(comp.equals(""))comp=author;
			else if(comp.indexOf(author)==-1)comp=comp+","+author;			
			row=Integer.parseInt(wr_meta[0][4]);
			dbd.UpdateSQl("sys_weekreport", row, "complete", comp);
			dbd.UpdateSQl("sys_weekreport", row, "author", authorlist);
			author_list=authorlist;
			String[] aus=comp.split(",");
			author_list=author_list+",";
			for(int i=0;i<aus.length;i++) {
				aus[i]=aus[i]+",";
				if(author_list.indexOf(aus[i])>-1)author_list=author_list.replace(aus[i], "");
			}			
			if(author_list.equals(""))dbd.UpdateSQl("sys_weekreport", row, "status", "已完成");
		} catch(JSONException e) {
			logger.error("周报数据内容不完整",e);
			throw new Exception("[info]412,周报数据内容不完整");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：更新管理员调整周报的结果, 只有管理员可以编辑，只能更新现有项目，不能增加过减少
	 * @param 	usr			发起操作请求的用户名
	 * @param 	WRname	周报名称
	 * @param 	WRdata		要更新的周报数据
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception 	500,系统错误
	 * usr,wrname,body
	 */
	void Savewr(String usr, String WRname,String WRdata) throws Exception {
		PropertyConfigurator.configure(logconf);			
//		周报表的字段包括："id","dept2","workitem_no","workitem","content","author","issue"
		try {			
//			检查要更新的周报是否存在
			String[][] wr_meta=dbd.readDB("sys_weekreport", "wr_index,type,author,complete,id", "fname='"+WRname+"'");
			if(wr_meta[0][0].equals("")) throw new Exception("[info]404,["+WRname+"]不存在");

			JSONObject wrd=new JSONObject(WRdata);
			JSONArray wr_line=wrd.getJSONArray("wrs");
			String[] check_col= {"dept2","workitem_no"};
			String[] check_val=new String[check_col.length];
			for(int i=0;i<wr_line.length();i++) {
				JSONObject wr_item=wr_line.getJSONObject(i);
				check_val[0]=wr_item.getString("dept2");
				check_val[1]=wr_item.getString("workitem_no");
				int row=dbd.check(wr_meta[0][0],check_col, check_val);
				if(row>0) {
					dbd.UpdateSQl(wr_meta[0][0], row, "workitem",wr_item.getString("workitem"));
					dbd.UpdateSQl(wr_meta[0][0], row, "content",wr_item.getString("content"));
					dbd.UpdateSQl(wr_meta[0][0], row, "issue",wr_item.getString("issue"));
				}
			}
		} catch(JSONException e) {
			logger.error("周报数据内容不完整",e);
			throw new Exception("[info]412,周报数据内容不完整");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：删除指定的周报
	 * @param 	usr			发起操作请求的用户名
	 * @param 	WRname	周报名称
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception	500,数据库故障
	 */
	void Delwr(String usr,String WRname)throws Exception  {
		PropertyConfigurator.configure(logconf);	
		try {
			String[] usrinfo=user.Get(usr);			
			String[][] wr_meta=dbd.readDB("sys_weekreport", "wr_index,type,owner,id", "fname='"+WRname+"'");
			if(wr_meta[0][0].equals("")) throw new Exception("[info]404,["+WRname+"]不存在");
				
			String own=usrinfo[0];
//			个人周报只能被owner删除，部门周报只能被部门管理员删除
			if(wr_meta[0][1].equals("dept")) {
				own=usrinfo[2];
				if(!usrinfo[5].equals("dept_admin"))throw new Exception("[info]401,您没有权限删除该报告");
			}
			if(!wr_meta[0][2].equals(own))throw new Exception("[info]401,您没有权限删除该报告");
			
//			删除周报表和列表记录
			int row=Integer.parseInt(wr_meta[0][3]);
			dbd.DelSQl("sys_weekreport", row, 1, 1);
			dbd.DelTable(wr_meta[0][0]);
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}	
	
	/**
	 * 函数说明：发布周报，向相关人员发送通知邮件
	 * @param  usr				当前操作用户
	 * @param  WRname		周报名称
	 * @throws Exception		401，权限不足
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		500，系统故障
	 */
	void ReleaseWR(String usr,String WRname) throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {
//			1. 权限判断，只有部门管理员才有权限发布周报
			String[] usrinfo=user.Get(usr);			
			String[][] wr_meta=dbd.readDB("sys_weekreport", "wr_index,type,owner,id,author", "fname='"+WRname+"'");
			if(wr_meta[0][0].equals("")) throw new Exception("[info]404,["+WRname+"]不存在，请联系管理员");
			if(!wr_meta[0][1].equals("dept"))	throw new Exception("[info]409,个人周报不能够发布");
			if(!usrinfo[5].equals("dept_admin"))throw new Exception("[info]401,无权操作");		
			if(!wr_meta[0][2].equals(usrinfo[2]))throw new Exception("[info]401,无权操作");
			
//			获取周报中的二级部门列表
			String[][] wr=dbd.readDB(wr_meta[0][0], "dept2", "id>0");
			String depts="";
			String dp="";
			for(int i=0;i<wr.length;i++) {
				dp="["+wr[i][0]+"]";
				if(depts.indexOf(dp)==-1)depts=depts+dp+",";
			}
			depts=depts.substring(0, depts.length()-1);
			depts=depts.replace("[", "");
			depts=depts.replace("]", "");
			String[] deptlist=depts.split(",");			
			
//			2. 邮件格式初始化
			String Mail_Cont="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			Mail_Cont=Mail_Cont+"\n"+"<title>"+WRname+"</title><style type='text/css'>";
			Mail_Cont=Mail_Cont+"\n"+"th {font-family:'微软雅黑';	font-size: 12px;border-bottom:1px solid;border-top:1px solid;"
					+ "height:25px;text-align:left;padding-left:5px;line-height:25px;}";
			Mail_Cont=Mail_Cont+"\n"+"td {font-family:'微软雅黑';font-size: 12px;	height:25px;line-height:25px;padding-left:5px;padding-right:5px;}"
					+ ".right{border-right:solid windowtext 1.0pt;border-bottom:solid windowtext 1.0pt;background-color: #FBDBDB;}";
			Mail_Cont=Mail_Cont+"\n"+".dept{border-bottom:solid windowtext 1.0pt;border-left:solid windowtext 1.0pt;}.bottomline{border-bottom:1px solid;}"
					+ "span{font-family:'微软雅黑';font-size: 12px;}";
			Mail_Cont=Mail_Cont+"\n"+"</style></head><body><div><span></span></div>";
			Mail_Cont=Mail_Cont+"\n"+"<div style='padding-left:30px'><table width=1000 style='width:746.0pt;margin-left:-1.15pt;border-collapse:collapse' "
					+ "cellspacing='0' cellpadding='0'><thead><th width=180 style='border-left:solid windowtext 1.0pt'>部门名称</th>"
					+ "<th width=240>工作</th><th width=340>内容</th><th width=240 class='right' style='background-color: #FFFFFF'>问题和建议</th></thead><tbody>";
			
//			3. 读取周报内容
			String filter="";
			String bcorlor="";
			String issues="";
			String templine="";
			for(int i=0;i<deptlist.length;i++) {
				if(bcorlor.equals(""))bcorlor="style='background-color:#E0E0E0'";
				else bcorlor="";
				filter="dept2='"+deptlist[i]+"'";
				wr=dbd.readDB(wr_meta[0][0], "workitem_no,workitem,content,issue", filter);
				Mail_Cont=Mail_Cont+"\n"+"<tr><td rowspan="+wr.length+" class='dept'><b>"+deptlist[i]+"</b></td>";
				if(wr.length==1) {
					Mail_Cont=Mail_Cont+"<td class='bottomline'"+bcorlor+">"+wr[0][0]+" "+wr[0][1]+"</td><td class='bottomline' "+bcorlor+">"+wr[0][2]+"</td>";
				}						
				else {
					Mail_Cont=Mail_Cont+"<td "+bcorlor+">"+wr[0][0]+" "+wr[0][1]+"</td><td "+bcorlor+">"+wr[0][2]+"</td>";
				}
				issues="";
				templine="";
				for(int j=1;j<wr.length;j++) {
					if(bcorlor.equals(""))bcorlor="style='background-color:#E0E0E0'";
					else bcorlor="";
					if(j<wr.length-1)templine=templine+"<tr><td "+bcorlor+">"+wr[j][0]+" "+wr[j][1]+"</td><td "+bcorlor+">"+wr[j][2]+"</td></tr>";
					else templine=templine+"<tr><td class='bottomline' "+bcorlor+">"+wr[j][0]+" "+wr[j][1]+"</td><td class='bottomline' "+bcorlor+">"+wr[j][2]+"</td></tr>";
					if(!wr[j][3].equals(""))issues=issues+wr[j][3]+"<br>";
				}
				if(!wr[0][3].equals(""))issues=wr[0][3]+"<br>"+issues;
				Mail_Cont=Mail_Cont+"<td rowspan="+wr.length+" class='right' valign='top'>"+issues+"</td></tr>";
				Mail_Cont=Mail_Cont+"\n"+templine;
			}			
			Mail_Cont=Mail_Cont+"\n"+"</tbody></table></div><div style='margin-top:30px;'><hr width='300px' align='left'></hr>";
			Mail_Cont=Mail_Cont+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span>";
			Mail_Cont=Mail_Cont+"\n"+"</div></body></html>";
			
//			4.发邮件通知
			String Receivers=user.getmail(wr_meta[0][4]);
			String[][] qunmail=dbd.readDB("sys_usrdb", "email", "role='deptmail' and dept1='"+wr_meta[0][2]+"'");
			if(!qunmail[0][0].equals(""))Receivers=Receivers+","+qunmail[0][0];
			qunmail=dbd.readDB("sys_usrdb", "email", "role='VP'");
			if(!qunmail[0][0].equals("")) {
				for(int i=0;i<qunmail.length;i++)	Receivers=Receivers+","+qunmail[i][0];
			}
			String Sender=user.getmail(usr);
			String Subject=WRname;
			String Texttype="text/html;charset=UTF-8";
			inf.toemail(Receivers, Sender,Subject, Mail_Cont, Texttype, "");
			
//			5. 更新周报状态
			int row=Integer.parseInt(wr_meta[0][3]);
			dbd.UpdateSQl("sys_weekreport", row, "status", "已发布");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**[Function] 			获取一周的周一到周日日期
	 * @param Y			年
	 * @param W		周编号
	 * @return				字符串日期，如"20170102-20170109"
	 * @throws Exception 
	 */
	String GetWeekdate(String Y,String W) throws Exception {	
		try {
			Date today = new Date();
	        Calendar c=Calendar.getInstance();
	        c.setTime(today);
			if(Y.equals("")) Y=""+c.getWeekYear();
			if(W.equals(""))W=""+c.get(Calendar.WEEK_OF_YEAR);
			int ww=Integer.parseInt(W)-1;
			String firstd = Y+"0101";
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat format_1 = new SimpleDateFormat("yyyy年MM月dd日");
			SimpleDateFormat format_2 = new SimpleDateFormat("MM月dd日");
			Date date;
			date = format.parse(firstd);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int  weekday=calendar.get(Calendar.DAY_OF_WEEK);
			int fla=ww * 7-weekday+2;
			calendar.add(Calendar.DATE,fla );
			String monday=format_1.format(calendar.getTime());
			calendar.add(Calendar.DATE, 4);
			String frifay=format_2.format(calendar.getTime());
			return monday+"-"+frifay;
		} catch (ParseException e) {
			logger.error(e.toString());
			throw new Exception(e);
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