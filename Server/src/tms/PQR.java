/**
 * 本模块提供的API包括：
 * 1. AddPQR(String usr,String PQRdata) 
 * 2. ListPQR(String filter,String Page_count,String Page_num)
 * 3. GetPQR(String PQR_index)
 * 4. ApprPQR(String usr,String PQR_index)
 * 5. UpdatePQR(String usr,String PQR_index,String body)
 * 6. DelPQR(String usr,String PQR_index)
 * 7. GetDQR(String proj, String version)
 */
package tms;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.*;

import base.*;

public class PQR {
	XMLDriver xml=new XMLDriver();
	DBDriver dbd = new DBDriver();
	User user=new User();
	Inform inf=new Inform();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	static String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(PQR.class.getName());
	
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DecimalFormat df=new DecimalFormat("#.##"); 
	
//	设置质量报告字段
	String[] col_Summ_table= {"prodline","vers_test","vers_pass","bug_close","bug_total","ver_pass_rate","bug_close_rate","PQI","bug_unclose"};
	String[] col_intest_proj_table= {"Test_proj","Test_cycle","Version","Bug_Sum","OpenBug","Rate_passTC","Reopen_Rate","PM"};
	String[] pqr= {"PQR_index","PQR_name","author", "creat_time", "pqr_STtime", "pqr_ENDtime", "pqr_period","QualRisk"};
	String[] state_analyze= {"sum_bugnum","rate_bug_l45","reject_version","last_rate_bug_l45","last_aver_bugnum","toler_bug_l45","toler_aver_timecost","aver_timecost"};
	
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
		String PQR_index=checkpara(Param,"PQR_index");
		String usr=checkpara(Param,"user");
		String code="412";
		String backvalue="412,http 请求的参数缺失或无效";
		
//		处理API
		try {
			switch(API){
			case "Add":   	
				String st=checkpara(Param,"pqr_st");
				String et=checkpara(Param,"pqr_et");
				if(!PQR_index.equals("")&&!st.equals("")&!et.equals("")) {
					logger.info("创建新质量报告"+PQR_index+"...");
					AddPQR(usr,PQR_index,st,et);			//需要权限控制
					return GetPQR(PQR_index);
				}				
			case "GetList":
				logger.info("获取质量报告列表...");
				return ListPQR();
			case "Get":   						
				if(!PQR_index.equals("")) {
					logger.info("获取质量报告"+PQR_index+"...");
					return GetPQR(PQR_index);
				}
				break;
			case "Release":   						
				PQR_index=checkpara(Param,"PQR_index");
				if(!PQR_index.equals("")) {
					logger.info("批准质量报告"+PQR_index+"...");
					ReleasePQR(usr,PQR_index);
					backvalue="200,ok";
				}
				break;
			case "Update":   						
				PQR_index=checkpara(Param,"PQR_index");
				if((!PQR_index.equals(""))&&(!body.equals(""))) {
					logger.info("更新质量报告"+PQR_index+"...");
					UpdatePQR(usr, PQR_index,body); 		//需要权限控制
					backvalue="200,ok";
				}
				break;
			case "GetPQD":   		
				String proj=checkpara(Param,"Proj_Name");
				String vers=checkpara(Param,"Proj_Version");
				if(!proj.equals("")&&!vers.equals("")) {
					logger.info("获取项目"+proj+vers+"的质量数据");
					return GetPQD(proj,vers);	
				}
				break;
			case "Delete":   		
				PQR_index=checkpara(Param,"PQR_index");
				if(!PQR_index.equals("")) {
					logger.info("删除质量报告"+PQR_index+"...");
					DelPQR(usr,PQR_index);		//需要权限控制
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
		code=backvalue.substring(0,backvalue.indexOf(","));
		message=backvalue.substring(backvalue.indexOf(",")+1);
		backvalue="{\"code\":"+code+",\"message\":\""+message+"\"}";
		return backvalue;
	}
	
	/**
	 * 函数说明：创建一个新的质量报告
	 * @param usr				当前操作的用户
	 * @param PQR_index	新质量报告的编号
	 * @param STtime			报告数据采集起始日期
	 * @param EDtime			报告数据采集终止日期
	 * @throws Exception 	401,权限不足
	 * @throws Exception 	409,报告已存在
	 * @throws Exception 	500,数据库故障
	 */
	public void AddPQR(String usr,String PQR_index,String STtime,String EDtime) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			只有测试经理和测试工程师才有权限创建新质量报告
			String usr_title=user.GetTitle(usr);			
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
			
//			报告周期参数处理
			int index=PQR_index.indexOf("_")+1;
			String Fyear=PQR_index.substring(index, index+4);
			String Fmonth=PQR_index.substring(index+4, PQR_index.length());
			String perio=Fmonth.substring(Fmonth.length()-1);
			Fmonth=Fmonth.substring(0, Fmonth.length()-1);
			String period="月";
			if(perio.equals("Q"))period="季度";		
			String PQR_fname="产品质量审计报告_"+Fyear+"年"+Fmonth+period;
//			判断质量报告是否已存在
			int row=dbd.check("sys_qualityreport", "PQR_index", PQR_index);
			if(row>0)throw new Exception("[info]409,"+PQR_fname+"已存在！");
//			如果是季度报告，需要先生成本季度最后一个月的月报
			int fmon=Integer.parseInt(Fmonth)*3;
			if(perio.equals("Q")) {
				String fnm="产品质量审计报告_"+Fyear+"年"+fmon+"月";
				row=dbd.check("sys_qualityreport", "PQR_name", fnm);
				if(row==0)throw new Exception("[info]409,请先创建"+Fyear+"年"+fmon+"月份的质量报告！");
			}			
//			判断产品线数据和本期测试报告是否正常
			String[][] pls=dbd.readDB("sys_prodline", "product_line", "id>0");
			if(pls[0][0].equals(""))throw new Exception("[info]404,系统中不存在任何产品线数据，请创建产品线后再尝试！");
			
			String stt=STtime.replace("年", "-");
			stt=stt.replace("月", "-");
			stt=stt.replace("日", "");
			String ett=EDtime.replace("年", "-");
			ett=ett.replace("月", "-");
			ett=ett.replace("日", "");
			ett=ett+" 23:59:59";
			String Curr_time=sdf_full.format(new Date());
			int pl_coun=pls.length;
			String filter="Tr_time>='"+stt+"' and Tr_time<='"+ett+"'";							
			String[][] projs=dbd.readDB("sys_testreport", "proj", filter);		
			if(projs[0][0].equals(""))throw new Exception("[info]404,没有找到本时间段内的测试报告数据，无法创建质量报告！");	
//			1. 创建质量报告数据文件
			String ftemp=confpath+"\\conf\\ASWS\\PQR_temp.xml";
			String filename=confpath+"\\PQR\\"+PQR_index+".xml";
			File tempfile = new File(ftemp);
			if (!tempfile.exists()) throw new Exception("[info]404,质量报告模板文件不存在或损坏！");
			tempfile = new File(filename);
//			如果报告已存在则需要删除重建
			if (tempfile.exists()) xml.Remove(filename);
			xml.Create(ftemp, filename);
			
//			2. 计算本季度质量数据
//			2.1 计算项目质量数据
			String projlist="";
			String ps="";
			int sum_test_cycle=projs.length;		//总测试轮数
			for(int i=0;i<sum_test_cycle;i++) {				
				if(!projs[i][0].equals(ps)) {
					ps="["+projs[i][0]+"]";
					if(projlist.indexOf(ps)==-1)	projlist=projlist+ps+",";
				}
			}
			projlist=projlist.replace("[", "");
			projlist=projlist.replace("]", "");
			projlist=projlist.substring(0, projlist.length()-1);
			String[] projects=projlist.split(",");
			String TRcol="productline,version,Num_bug_total,Num_bug_unclose,Num_dqs,Num_tc_exe,Num_tc_fail,Num_bug_reopen,Num_bug_ul3,Timecost,testresult";
			String[][] proj_data=new String[projects.length][10];		
//			proj_data字段：项目、轮数、产品线、版本、总Bug数、未关闭Bug数、DQS、用例通过率、Bug重开率，测试通过数
			int sum_timecost=0;			//总版本测试耗时
			int sum_bugnum_ul3=0;		//总高级Bug数
			int sum_bugnum=0;			//总Bug数
			int sum_reject_version=0;	//总驳回版本
			int sum_bugnum_proj=0; 	//项目Bug总数
			for(int i=0;i<projects.length;i++) {
				sum_bugnum_proj=0;
				proj_data[i][0]=projects[i];				
				filter="proj='"+projects[i]+"' and Tr_time>='"+stt+"' and Tr_time<='"+ett+"' order by Tr_time desc";							
				String[][] TR_data=dbd.readDB("sys_testreport", TRcol, filter);	
				if(!TR_data[0][0].equals("")) {
//					可以直接取值的字段：测试轮数、产品线、版本、总Bug数、未关闭Bug数、DQS
					proj_data[i][1]=""+TR_data.length;	
					for(int k=0;k<5;k++)proj_data[i][k+2]=TR_data[0][k];
//					考虑到周期内多个大测试版本的情况，每个版本的总Bug数都是独立计算，所以需要从新计算Bug总数
					String tag_ver=TR_data[0][1];
					int sum_bug_temp=Integer.parseInt(TR_data[0][2]);
					for(int k=1;k<TR_data.length;k++) {
						if(!TR_data[k][1].equals(tag_ver)) {
							sum_bug_temp=sum_bug_temp-Integer.parseInt(TR_data[k][3])+Integer.parseInt(TR_data[k][2]);
							tag_ver=TR_data[k][1];
						}
					}
					proj_data[i][4]=""+sum_bug_temp;
					sum_bugnum=sum_bugnum+sum_bug_temp;
					sum_bugnum_proj=sum_bugnum_proj+sum_bug_temp;
					sum_bugnum_ul3=sum_bugnum_ul3+Integer.parseInt(TR_data[0][8]);
					int tcexe=0;				//用例执行数
					int tcpass=0;				//用例通过数
					int bug_reopen=0;		//重开Bug数	
					int pass_ver=0;			//通过版本数
					for(int k=0;k<TR_data.length;k++) {
						tcexe=tcexe+Integer.parseInt(TR_data[k][5]);
						tcpass=tcpass+Integer.parseInt(TR_data[k][5])-Integer.parseInt(TR_data[k][6]);
						bug_reopen=bug_reopen+Integer.parseInt(TR_data[k][7]);	
						sum_timecost=sum_timecost+Integer.parseInt(TR_data[k][9]);	
						if(TR_data[k][10].equals("pass"))pass_ver++;
						else if(TR_data[k][10].equals("reject"))sum_reject_version++;
					}
//					计算用例通过率、Bug重开率
					proj_data[i][7]="0";
					proj_data[i][8]="0";
					if(tcexe>0)proj_data[i][7]=df.format(100*(float)tcpass/tcexe);
					if(sum_bugnum_proj>0) proj_data[i][8]=df.format(100*bug_reopen/sum_bugnum_proj);
					proj_data[i][9]=""+pass_ver;
				}
			}
			
//			2.2 计算产品线质量数据
			String[][] pl_data=new String[pl_coun+1][9];
			int sum_pass_ver=0;
			int sum_bug_unclose=0;
			int sum_bug_close=0;
//			pl_data字段：产品线、轮数(版本数)、通过版本数、关闭Bug数、Bug总数、版本通过率、Bug解决率、PQI、未关闭Bug
			for(int i=0;i<pl_coun;i++) {
				pl_data[i][0]=pls[i][0];
				int pl_test_cycle=0;
				int pl_pass_ver=0;
				int pl_bug_unclose=0;
				int pl_bug_sum=0;
				for(int k=0;k<proj_data.length;k++) {
					if(pl_data[i][0].equals(proj_data[k][2])) {
						pl_test_cycle=pl_test_cycle+Integer.parseInt(proj_data[k][1]);	
						pl_pass_ver=pl_pass_ver+Integer.parseInt(proj_data[k][9]);	
						pl_bug_unclose=pl_bug_unclose+Integer.parseInt(proj_data[k][5]);	
						pl_bug_sum=pl_bug_sum+Integer.parseInt(proj_data[k][4]);	
					}				
				}
				pl_data[i][1]=""+pl_test_cycle;
				pl_data[i][2]=""+pl_pass_ver;	
				sum_pass_ver=sum_pass_ver+pl_pass_ver;
				pl_data[i][4]=""+pl_bug_sum;
				pl_data[i][8]=""+pl_bug_unclose;
				sum_bug_unclose=sum_bug_unclose+pl_bug_unclose;
				pl_data[i][3]=""+(pl_bug_sum-pl_bug_unclose);
				sum_bug_close=sum_bug_close+(pl_bug_sum-pl_bug_unclose);
				pl_data[i][5]="0";
				if(pl_test_cycle>0)pl_data[i][5]=""+df.format(100*(float)pl_pass_ver/pl_test_cycle);
				pl_data[i][6]="0";
				if(pl_bug_sum>0)pl_data[i][6]=""+df.format(100*(float)(pl_bug_sum-pl_bug_unclose)/pl_bug_sum);
				pl_data[i][7]=""+df.format(Float.parseFloat(pl_data[i][5])*Float.parseFloat(pl_data[i][6])/10000);
			}
			pl_data[pl_coun][0]="综合指标";
			pl_data[pl_coun][1]=""+sum_test_cycle;
			pl_data[pl_coun][2]=""+sum_pass_ver;
			pl_data[pl_coun][3]=""+sum_bug_close;
			pl_data[pl_coun][4]=""+sum_bugnum;
			pl_data[pl_coun][5]="0";
			if(sum_test_cycle>0)pl_data[pl_coun][5]=""+df.format(100*(float)sum_pass_ver/sum_test_cycle);
			pl_data[pl_coun][6]="0";
			if(sum_bugnum>0)pl_data[pl_coun][6]=""+df.format(100*(float)sum_bug_close/sum_bugnum);
			pl_data[pl_coun][7]=""+df.format(Float.parseFloat(pl_data[pl_coun][5])*Float.parseFloat(pl_data[pl_coun][6])/10000);		
			pl_data[pl_coun][8]=""+sum_bug_unclose;	
			
			String[] colname= {"PQR_index","PQR_name","Fyear","Fmonth","create_time","author"};
			String[] record=new String[colname.length];
			record[0]=PQR_index;
			record[1]=PQR_fname;
			record[2]=Fyear;
			record[3]=Fmonth+perio;
			record[4]=sdf_full.format(new Date());
			
//			3. 写入基本信息数据
			String[] usrinfo=user.Get(usr);
			String[] baseinfo=new String[pqr.length];
			baseinfo[0]=PQR_index;
			baseinfo[1]=PQR_fname;
			baseinfo[2]=usrinfo[1];
			record[5]=usrinfo[1];
			baseinfo[3]=Curr_time;
			baseinfo[4]=STtime;
			baseinfo[5]=EDtime;
			baseinfo[6]=period;		
			baseinfo[7]="";	
			for(int i=0;i<pqr.length;i++) xml.Update(filename, pqr[i],baseinfo[i]);	
//			4. 写入概要数据表
			for(int i=0;i<=pl_coun;i++) {
				xml.Add(filename, "Summ_table", "item"+(i+1), "");
				for(int k=0;k<9;k++) xml.Add(filename, "Summ_table/item"+(i+1), col_Summ_table[k], pl_data[i][k]);
			}			
//			5. 写入项目数据表
			String[] project_data=new String[col_intest_proj_table.length];			
			String proj_chart_times="";		//项目图表系列值
			String chart_tpd_dat="";			//项目测试轮数
			String chart_bsd_dat="";			//项目DQS
			String chart_brd_dat="";			//项目Bug重开率
			String chart_bld_dat="";			//项目待解决Bug
			for(int i=0;i<proj_data.length;i++) {
//				proj_data[][]中有部分数据不用写入项目表，所以这里做一次过滤
				int n=0;
				for(int k=0;k<9;k++) {
					if(k==2 || k==6)continue;
					project_data[n]=proj_data[i][k];
					n++;
				}
				String[][] pm=dbd.readDB("sys_test_proj", "pm", "proj='"+project_data[0]+"' and version='"+project_data[2]+"'");
				project_data[7]=pm[0][0];
				xml.Add(filename, "intest_proj_table", "item"+(i+1), "");
				for(int k=0;k<col_intest_proj_table.length;k++) {
					xml.Add(filename, "intest_proj_table/item"+(i+1), col_intest_proj_table[k], project_data[k]);
				}
//				计算项目图表数据
				proj_chart_times=proj_chart_times+proj_data[i][0]+",";
				chart_tpd_dat=chart_tpd_dat+proj_data[i][1]+",";
				chart_bsd_dat=chart_bsd_dat+proj_data[i][6]+",";
				chart_brd_dat=chart_brd_dat+proj_data[i][8]+",";
				chart_bld_dat=chart_bld_dat+proj_data[i][5]+",";
			}
			
//			6. 写入项目图表数据
			proj_chart_times=proj_chart_times.substring(0, proj_chart_times.length()-1);
//			6.1 写回归次数图 chart_intest_tpd
			xml.Update(filename, "chart/chart_intest_tpd/times", proj_chart_times);
			xml.Update(filename, "chart/chart_intest_tpd/dats/dat", chart_tpd_dat.substring(0, chart_tpd_dat.length()-1));
//			6.2 写DQS表 chart_intest_bsd
			xml.Update(filename, "chart/chart_intest_bsd/times", proj_chart_times);
			xml.Update(filename, "chart/chart_intest_bsd/dats/dat", chart_bsd_dat.substring(0, chart_bsd_dat.length()-1));
//			6.3 写Bug重开率图 chart_intest_brd	
			xml.Update(filename, "chart/chart_intest_brd/times", proj_chart_times);
			xml.Update(filename, "chart/chart_intest_brd/dats/dat", chart_brd_dat.substring(0, chart_brd_dat.length()-1));
//			6.4 写待解决Bug分布图 chart_intest_bld	
			xml.Update(filename, "chart/chart_intest_bld/times", proj_chart_times);
			xml.Update(filename, "chart/chart_intest_bld/dats/dat", chart_bld_dat.substring(0, chart_bld_dat.length()-1));
			
//			7. 获取上一期报告数值，包括：平均测试耗时、平均测试轮数、人均bug数、45级bug占比
//			state_analyze= {"sum_bugnum","rate_bug_l45","reject_version","last_rate_bug_l45","last_aver_bugnum","toler_bug_l45","toler_aver_timecost","aver_timecost"};
			String[] state_analyze_data=new String[state_analyze.length];
			state_analyze_data[0]=""+sum_bugnum;
			state_analyze_data[1]=""+df.format(100*(float)sum_bugnum_ul3/sum_bugnum);
			state_analyze_data[2]=""+sum_reject_version;
//			计算本期的平均耗时和平均回归次数
			String aver_cycle="0";
			if(proj_data.length>0)	aver_cycle=df.format((float)sum_test_cycle/proj_data.length);
			String aver_timecost="0";
			if(sum_test_cycle>0)aver_timecost=df.format((float)sum_timecost/sum_test_cycle);
			state_analyze_data[7]=aver_timecost;
			
			String newFmonth=""+(Integer.parseInt(Fmonth)-1);
			String newFyear=Fyear;
			if(Fmonth.equals("1")) {
				newFyear=""+(Integer.parseInt(newFyear)-1);
				if(perio.equals("Q"))newFmonth="4";
				else newFmonth="12";
			}
			String last_PQR_index="PQR_"+newFyear+newFmonth+perio;
			String lastfilename=confpath+"\\PQR\\"+last_PQR_index+".xml";
			File lastPQR = new File(lastfilename);
//			如果上一期文件不存在则将数据置0
			if (!lastPQR.exists()) {
				state_analyze_data[3]="0";
				state_analyze_data[4]="0";
				state_analyze_data[5]="0";
				state_analyze_data[6]="0";
//				设置产品测试周期图数据 chart_tpd				
				xml.Update(filename, "chart/chart_tpd/times", Fmonth+period);
				xml.Update(filename, "chart/chart_tpd/dats/item1/dat", aver_timecost);
				xml.Update(filename, "chart/chart_tpd/dats/item2/dat", aver_cycle);
//				设置PQI图表数据 chart_pqi
				if(perio.equals("M")) {
					xml.Update(filename, "chart/chart_pqi/times", Fmonth+period);
					for(int i=0;i<pl_data.length;i++) {
						xml.Add(filename, "chart/chart_pqi/dats", "item"+(i+1), "");
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "seriers", pl_data[i][0]);
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "type", "line");
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "dat", pl_data[i][7]);
					}
				}
			}
			else {
				state_analyze_data[3]=xml.GetNode(lastfilename, "state_analyze/rate_bug_l45");
				state_analyze_data[4]=xml.GetNode(lastfilename, "state_analyze/aver_bugnum");
				if(state_analyze_data[3].equals("0"))state_analyze_data[5]="0";
				else {
					float Bug_ul3_rate=Float.parseFloat(state_analyze_data[1]);
					float Bug_ul3_rate_last=Float.parseFloat(state_analyze_data[3]);
					state_analyze_data[5]=df.format(100*(Bug_ul3_rate-Bug_ul3_rate_last)/Bug_ul3_rate_last);
				}				
				String last_aver_timecost=xml.GetNode(lastfilename, "state_analyze/aver_timecost");
				if(last_aver_timecost.equals("0"))state_analyze_data[6]="0";
				else {
					float aver_tc=Float.parseFloat(aver_timecost);
					float aver_tc_last=Float.parseFloat(last_aver_timecost);
					state_analyze_data[6]=df.format(100*(aver_tc-aver_tc_last)/aver_tc_last);
				}
				
//				设置产品测试周期图数据 chart_tpd
				String last_times=xml.GetNode(lastfilename,  "chart/chart_tpd/times");
				xml.Update(filename, "chart/chart_tpd/times", last_times+","+Fmonth+period);
				String last_dat1=xml.GetNode(lastfilename,  "chart/chart_tpd/dats/item1/dat");
				String last_dat2=xml.GetNode(lastfilename,  "chart/chart_tpd/dats/item2/dat");
				xml.Update(filename, "chart/chart_tpd/dats/item1/dat", last_dat1+","+aver_timecost);
				xml.Update(filename, "chart/chart_tpd/dats/item2/dat", last_dat2+","+aver_cycle);
				
//				设置PQI图表数据 chart_pqi
				if(perio.equals("M")) {
					last_times=xml.GetNode(lastfilename,  "chart/chart_pqi/times");
					xml.Update(filename, "chart/chart_pqi/times", last_times+","+Fmonth+period);
					String[] pref=(last_times+","+Fmonth+period).split(",");
					String temp_dat="";
					String pare_node="";
					for(int i=0;i<pref.length;i++)temp_dat=temp_dat+"0,";
					for(int i=0;i<pl_data.length;i++) {
						xml.Add(filename, "chart/chart_pqi/dats", "item"+(i+1), "");
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "seriers", pl_data[i][0]);
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "type", "line");
						pare_node=xml.GetParentNode(lastfilename,"seriers",pl_data[i][0]);  
						if(!pare_node.equals("")) {
							temp_dat=xml.GetNode(lastfilename, "chart/chart_pqi/dats/"+pare_node+"/dat");							
						}		
						xml.Add(filename, "chart/chart_pqi/dats/item"+(i+1), "dat", temp_dat+","+pl_data[i][7]);
					}
				}
			}
//			写入state_analyze_data数据
			for(int i=0;i<state_analyze.length;i++)xml.Update(filename, "state_analyze/"+state_analyze[i], state_analyze_data[i]);
			
//			如果是季度，则读取本季度最后一个月的PQI图表数据
			if(perio.equals("Q")) {
				last_PQR_index="PQR_"+Fyear+fmon+"M";
				lastfilename=confpath+"\\PQR\\"+last_PQR_index+".xml";					
				String last_times = xml.GetNode(lastfilename,  "chart/chart_pqi/times");
				xml.Update(filename, "chart/chart_pqi/times", last_times);
//				拷贝本季度最后一个月份的PQI图表数据
				String itemlist=xml.GetListA(lastfilename, "chart/chart_pqi/dats");
				itemlist=itemlist.replace(":", "");
				itemlist=itemlist.replace("\"", "");
				itemlist=itemlist.substring(1, itemlist.length()-1);
				String[] item=itemlist.split(",");
				String temp_seriers="";
				String temp_dat="";
				for(int i=0;i<item.length;i++) {
					xml.Add(filename, "chart/chart_pqi/dats", item[i], "");
					temp_seriers=xml.GetNode(lastfilename, "chart/chart_pqi/dats/"+item[i]+"/seriers");
					temp_dat=xml.GetNode(lastfilename, "chart/chart_pqi/dats/"+item[i]+"/dat");
					xml.Add(filename, "chart/chart_pqi/dats/"+item[i],"seriers", temp_seriers);
					xml.Add(filename, "chart/chart_pqi/dats/"+item[i],"type", "line");
					xml.Add(filename, "chart/chart_pqi/dats/"+item[i],"dat", temp_dat);
				}
			}
			dbd.AppendSQl("sys_qualityreport", colname, record, 1, 1);
		} catch(JSONException e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}		
	}
	/**
	 * 函数说明：列出当前所有质量报告，只罗列质量报告简表
	 * @return	  JSONArray		格式字符串，包括字段Fyear, Ylist,
	 * @throws Exception 500,数据库故障
	 */
	String ListPQR() throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
			String col_table= "PQR_index,PQR_name,Fyear";
			String filter="id>0 order by create_time desc";
			JSONObject PQRlist=new JSONObject();
			JSONArray pqrs=new JSONArray();	
			String[][] PQRs=dbd.readDB("sys_qualityreport", col_table, filter);	
			int num=0;
			if(!PQRs[0][0].equals("")) {
				num=PQRs.length;
				int lastyear=Integer.parseInt(PQRs[num-1][2]);
				SimpleDateFormat sdf_y = new SimpleDateFormat("yyyy");
				Date d = new Date();  				
				int curryear=Integer.parseInt(sdf_y.format(d));
				int j=0;
				for(int i=curryear;i>=lastyear;i--) {
					filter="Fyear='"+i+"' order by create_time desc";  
					PQRs=dbd.readDB("sys_qualityreport", col_table, filter);	
					JSONObject pqr=new JSONObject();
					JSONArray ylist=new JSONArray();	
					pqr.put("Fyear", ""+i);
					if(!PQRs[0][0].equals(""))for(int k=0;k<PQRs.length;k++)ylist.put(k, PQRs[k][0]);
					pqr.put("Ylist",ylist);
					pqrs.put(j, pqr);
					j++;
				}
			}					
			PQRlist.put("pqrnum",num);					
			PQRlist.put("pqrs",pqrs);
			PQRlist.put("code",200);
			return PQRlist.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：获取指定的质量报告详细内容
	 * @param 	PQR_index	要获取的质量报告编号
	 * @throws 	Exception 	404,找不到质量报告
	 * @throws 	Exception 	500,系统错误
	 * @return		完整的JSON格式返回值
	 */
	public String GetPQR(String PQR_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		String filename=confpath+"\\PQR\\"+PQR_index+".xml";
		try {
			int row=dbd.check("sys_qualityreport", "PQR_index", PQR_index);
			if(row==0)throw new Exception("[info]404,没有找到质量报告"+PQR_index);
			File tempfile = new File(filename);
			if (!tempfile.exists()) throw new Exception("[info]404,报告"+PQR_index+"的数据文件不存在或损坏！");
			
			JSONObject PQR=new JSONObject();
//			1. 基本信息
			for(int i=0;i<pqr.length;i++)PQR.put(pqr[i], xml.GetNode(filename, pqr[i]));			
//			2. 分析数据
			JSONObject sa=new JSONObject();
			String[] state_analy= {"sum_bugnum","rate_bug_l45","reject_version","last_rate_bug_l45","last_aver_bugnum","toler_bug_l45","toler_aver_timecost",
					"aver_timecost","onlinever_untest","dev_num","aver_bugnum","toler_aver_bugnum","L1_bugnum","L2_bugnum","L3_bugnum","L4_bugnum","L5_bugnum"};
			for(int i=0;i<state_analy.length;i++)sa.put(state_analy[i], xml.GetNode(filename, "state_analyze/"+state_analy[i]));
			PQR.put("state_analyze", sa);			
//			3. 表格
//			3.1 概览表
			String temp=xml.GetList(filename, "Summ_table");
			PQR.put("Summ_table", new JSONArray(temp));
//			3.2 项目表
			temp=xml.GetList(filename, "intest_proj_table");
			PQR.put("intest_proj_table", new JSONArray(temp));
			
//			4. 图表
			JSONObject chart=new JSONObject();
//			4.1 PQI			
			JSONObject chart_pqi=new JSONObject();
			chart_pqi.put("title", xml.GetNode(filename, "chart/chart_pqi/title"));
			chart_pqi.put("times", xml.GetNode(filename, "chart/chart_pqi/times"));
			temp=xml.GetList(filename, "chart/chart_pqi/dats/");
			JSONArray chart_pqi_dats=new JSONArray(temp);	
			chart_pqi.put("dats", chart_pqi_dats);
			chart.put("chart_pqi", chart_pqi);
//			4.2 测试周期与耗时分布图
			JSONObject chart_tpd=new JSONObject();
			chart_tpd.put("title", xml.GetNode(filename, "chart/chart_tpd/title"));
			chart_tpd.put("times", xml.GetNode(filename, "chart/chart_tpd/times"));
			temp=xml.GetList(filename, "chart/chart_tpd/dats/");
			JSONArray chart_tpd_dats=new JSONArray(temp);				
			chart_tpd.put("dats", chart_tpd_dats);
			chart.put("chart_tpd", chart_tpd);
//			4.3 Bug级别分布图
			JSONObject chart_bsd=new JSONObject();
			chart_bsd.put("title", xml.GetNode(filename, "chart/chart_bsd/title"));
			chart_bsd.put("seriers", xml.GetNode(filename, "chart/chart_bsd/seriers"));
			temp=xml.GetList(filename, "chart/chart_bsd/dats/");
			JSONArray chart_bsd_dats=new JSONArray(temp);	
			chart_bsd.put("dats",chart_bsd_dats);
			chart.put("chart_bsd", chart_bsd);
//			4.4 项目回归次数分布
			JSONObject chart_intest_tpd=new JSONObject();
			chart_intest_tpd.put("title", xml.GetNode(filename, "chart/chart_intest_tpd/title"));
			chart_intest_tpd.put("times", xml.GetNode(filename, "chart/chart_intest_tpd/times"));
			temp=xml.GetListA(filename, "chart/chart_intest_tpd/dats/");
			JSONArray chart_intest_tpd_dats=new JSONArray();	
			chart_intest_tpd_dats.put(new JSONObject(temp));
			chart_intest_tpd.put("dats", chart_intest_tpd_dats);
			chart.put("chart_intest_tpd", chart_intest_tpd);
//			4.5 项目DQS分布
			JSONObject chart_intest_bsd=new JSONObject();
			chart_intest_bsd.put("title", xml.GetNode(filename, "chart/chart_intest_bsd/title"));
			chart_intest_bsd.put("times", xml.GetNode(filename, "chart/chart_intest_bsd/times"));
			temp=xml.GetListA(filename, "chart/chart_intest_bsd/dats/");
			JSONArray chart_intest_bsd_dats=new JSONArray();	
			chart_intest_bsd_dats.put(new JSONObject(temp));
			chart_intest_bsd.put("dats", chart_intest_bsd_dats);
			chart.put("chart_intest_bsd", chart_intest_bsd);
//			4.6 项目Bug重开率分布
			JSONObject chart_intest_brd=new JSONObject();
			chart_intest_brd.put("title", xml.GetNode(filename, "chart/chart_intest_brd/title"));
			chart_intest_brd.put("times", xml.GetNode(filename, "chart/chart_intest_brd/times"));
			temp=xml.GetListA(filename, "chart/chart_intest_brd/dats/");
			JSONArray chart_intest_brd_dats=new JSONArray();	
			chart_intest_brd_dats.put(new JSONObject(temp));
			chart_intest_brd.put("dats", chart_intest_brd_dats);
			chart.put("chart_intest_brd", chart_intest_brd);
//			4.7 项目待解决Bug分布
			JSONObject chart_intest_bld=new JSONObject();
			chart_intest_bld.put("title", xml.GetNode(filename, "chart/chart_intest_bld/title"));
			chart_intest_bld.put("times", xml.GetNode(filename, "chart/chart_intest_bld/times"));
			temp=xml.GetListA(filename, "chart/chart_intest_bld/dats/");
			JSONArray chart_intest_bld_dats=new JSONArray();	
			chart_intest_bld_dats.put(new JSONObject(temp));
			chart_intest_bld.put("dats", chart_intest_bld_dats);
			chart.put("chart_intest_bld", chart_intest_bld);
			PQR.put("chart", chart);
			PQR.put("code",200);
			return PQR.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	void ReleasePQR(String usr,String PQR_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			1. 权限审核，只有测试经理才有权限发布质量报告 
			String usr_title=user.GetTitle(usr);	
			if((!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
//			2. 检查报告是否存在
			int row=dbd.check("sys_qualityreport", "PQR_index", PQR_index);
			if(row==0)throw new Exception("[info]404,没有找到质量报告"+PQR_index);
			
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new Exception("[info]500,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：更新质量报告，只有测试经理和测试工程师才有权限修改质量报告，已发布报告不能修改和删除
	 * @param usr				当前操作的用户
	 * @param PQR_index		要更新的质量报告编号
	 * @param body			用来更新质量报告的数据，JSON格式
	 * @throws Exception 	404,测试项目不存在
	 * @throws Exception 	409,测试项目不存在
	 * @throws Exception 	500,数据库故障
	 */
	void UpdatePQR(String usr,String PQR_index,String body) throws Exception {
		PropertyConfigurator.configure(logconf);		
		String filename=confpath+"\\PQR\\"+PQR_index+".xml";
		try {
//			只有测试经理和测试工程师才有权限修改质量报告
			String usr_title=user.GetTitle(usr);	
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
			
			int row=dbd.check("sys_qualityreport", "PQR_index", PQR_index);
			if(row==0)throw new Exception("[info]404,没有找到质量报告"+PQR_index);
			File tempfile = new File(filename);
			if (!tempfile.exists()) throw new Exception("[info]404,报告"+PQR_index+"的数据文件不存在或损坏！");
//			更新质量报告内容
			String[] baseinfo_cols= {"pqr_period","pqr_STtime","pqr_ENDtime","QualRisk"};
			String[] state_analyze_cols= {"dev_num","aver_bugnum","toler_aver_bugnum","onlinever_untest","rate_bug_l45","toler_bug_l45"};	
			String[] Buglvs= {"L1_bugnum","L2_bugnum","L3_bugnum","L4_bugnum","L5_bugnum"};
			JSONObject PQR=new JSONObject(body);		
			for(int i=0;i<baseinfo_cols.length;i++)xml.Update(filename, baseinfo_cols[i], PQR.getString(baseinfo_cols[i]));
			JSONObject sa=PQR.getJSONObject("state_analyze");
			for(int i=0;i<state_analyze_cols.length;i++)xml.Update(filename, "state_analyze/"+state_analyze_cols[i], sa.getString(state_analyze_cols[i]));
			for(int i=0;i<Buglvs.length;i++)xml.Update(filename, "state_analyze/"+Buglvs[i], sa.getString(Buglvs[i]));
			for(int i=0;i<Buglvs.length;i++)xml.Update(filename, "chart_bsd/dats/item"+(i+1)+"/value", sa.getString(Buglvs[i]));		
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：删除指定的质量报告，只有测试经理和测试工程师才有权限修改质量报告，已发布报告不能修改和删除
	 * @param 	usr			当前操作的用户
	 * @param 	PQR_index	要删除的质量报告编号
	 * @throws 	Exception	404,测试项目不存在
	 * @throws 	Exception	500,数据库故障
	 */
	void DelPQR(String usr,String PQR_index) throws Exception  {
		PropertyConfigurator.configure(logconf);	
		String filename=confpath+"\\PQR\\"+PQR_index+".xml";
		try {
//			只有测试经理和测试工程师才有权限删除质量报告
			String usr_title=user.GetTitle(usr);	
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
			
//			判断报告是否存在
			int row=dbd.check("sys_qualityreport", "PQR_index", PQR_index);
			if(row==0)throw new Exception("[info]404,没有找到质量报告"+PQR_index);
			
//			删除报告文件和数据库记录
			File tempfile = new File(filename);
			if (tempfile.exists())	xml.Remove(filename);
			dbd.DelSQl("sys_qualityreport", row, 1, 1);
		} catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：获取指定项目的质量数据
	 * @param 	proj	指定的项目
	 * @param 	vers	项目版本
	 * @throws 	Exception 	404,找不到质量报告
	 * @throws 	Exception 	500,系统错误
	 * @return		完整的JSON格式返回值
	 */
	String GetPQD(String proj,String vers) throws Exception {
		PropertyConfigurator.configure(logconf);			
		try {
			JSONObject Tr=new JSONObject();
			JSONArray ja=new JSONArray();
			String[] PQD_field= {"Subversion","Update_time" ,"Reporter" ,"TestResult" ,"Num_tc_exe" ,"Num_tc_fail","Num_bug_unclose" ,"Num_bug_total","Num_bug_ul3",
										  "Num_bug_reopen","Num_bug_close","Num_dqs" ,"Rate_bug_open" ,"Rate_bug_reopen" ,"Rate_tc_fail" ,"Timecost","Cycles"};
			String cols="subversion,Tr_time,Reporter,testresult,Num_tc_exe,Num_tc_fail,Num_bug_unclose,Num_bug_total,Num_bug_ul3,Num_bug_reopen,"+
							 "Num_bug_close,Num_dqs,Rate_bug_open,Rate_bug_reopen,Rate_tc_fail,Timecost,Cycles";
			int num=0;
			String[][] trs=dbd.readDB("sys_testreport", cols, "proj='"+proj+"' and version='"+vers+"'");
			if(!trs[0][0].equals("")) {
				num=trs.length;
				for(int i=0;i<num;i++) {
					JSONObject pqd=new JSONObject();
					int num2=PQD_field.length;
					for(int j=0;j<num2;j++) {
						pqd.put(PQD_field[j], trs[i][j]);
					}
					ja.put(i,pqd);
				}
			}
			Tr.put("PQD_num", num);
			Tr.put("PQD", ja);
			Tr.put("code", 200);
			return Tr.toString();
		}catch (Throwable e) {
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