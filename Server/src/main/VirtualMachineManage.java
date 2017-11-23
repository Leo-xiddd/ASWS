package main;

import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import base.*;

public class VirtualMachineManage {
	DBDriver dbd = new DBDriver();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	static String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(ProjectManage.class.getName());
	
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
		String backvalue="412,http 请求的参数缺失或无效";
		String hostname=checkpara(Param,"hostname");
		String vm_name=checkpara(Param,"vm_name");
		String gateway=checkpara(Param,"gateway");
		String net=checkpara(Param,"net");
		try {
			switch(API){
			case "AddNetwork":	
				if(!gateway.equals("")) {
					logger.info("添加新网络...");
					AddNetwork(gateway);	
					backvalue="200,ok";
				}				
				break;
			case "ListNetwork":   	
				logger.info("列出"+net+"网络下的IP列表...");
				return ListNetwork(net);	
			case "DelNetwork":   		
				if(!net.equals("")) {
					logger.info("删除网络"+net+"...");
					DelNetwork(net);
					backvalue="200,ok";
				}
				break;
			case "AddServ":	
				if(!body.equals("")) {
					logger.info("添加新服务器...");
					AddServ(body);	
					backvalue="200,ok";
				}				
				break;
			case "ListServ":   	
				logger.info("列出所有服务器...");
				return ListServ();	
			case "GetServer":
				if(!hostname.equals("")) {
					logger.info("获取服务器"+hostname+"的信息及下属虚拟机列表...");
					return GetServer(hostname);	
				}
				break;
			case "DelServ":   		
				if(!hostname.equals("")) {
					logger.info("删除服务器"+hostname);
					DelServ(hostname);
					backvalue="200,ok";
				}
				break;
			case "UpdateServ":	
				if(!body.equals("") && !hostname.equals("")) {
					logger.info("修改服务器信息...");
					UpdateServ(hostname,body);	
					backvalue="200,ok";
				}				
				break;
			case "AddVM":	
				if(!body.equals("")) {
					logger.info("添加新虚拟机...");
					AddVM(body);	
					backvalue="200,ok";
				}				
				break;
			case "DelVM":   		
				if(!vm_name.equals("")) {
					logger.info("删除虚拟机"+vm_name);
					DelVM(vm_name);
					backvalue="200,ok";
				}
				break;
			case "UpdateVM":	
				if(!body.equals("") && !vm_name.equals("")) {
					logger.info("修改虚拟机信息...");
					UpdateVM(vm_name,body);	
					backvalue="200,ok";
				}				
				break;
			case "FindVM":
				String key=checkpara(Param,"key");
				String value=checkpara(Param,"value");
				if(!value.equals("") && !key.equals("")) {
					logger.info("查询"+key+"="+value+"的虚拟机...");
					return ListVM(key,value);	
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
	 * 函数说明：添加新的网段和IP池
	 * @param gateway	新网络的网关地址
	 * @throws Exception 409，网络已存在
	 */
	void AddNetwork(String gateway)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			String[] colname= {"network","gateway"};
			String[] record=new String[colname.length];
			record[0]=gateway.substring(0, gateway.lastIndexOf("."))+".0";
			record[1]=gateway;
			int row=dbd.check("vmm_network", "network", record[0]);
			if(row>0)throw new Exception("[info]409,已经存在网络"+record[0]+"，不能重复添加。");
			dbd.AppendSQl("vmm_network", colname, record, 1, 1);
//			创建IP池
			String[] cols= {"network","net","addr","state","des"};
			String[] reco=new String[cols.length];
			String[] net_item=gateway.split("\\.");		
			reco[1]=net_item[2];			
			String ip=net_item[0]+"."+net_item[1]+"."+net_item[2]+".";
			reco[0]=ip+"0";	
			for(int i=1;i<255;i++) {
				reco[3]="可用";
				reco[4]="";
				reco[2]=ip+i;
				if(reco[2].equals(gateway)) {
					reco[3]="已用";
					reco[4]="网关";
				}
				dbd.AppendSQl("vmm_ippool", cols, reco, 1, 1);
			}
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：列出网络和指定网络的IP池，未使用IP靠前
	 * @param net	指定要列出IP池的网络号，为空时列出第一个网络
	 * @return		JSON格式字符串，如：{"nets":["192.168.56.0","192.168.55.0",...],"ips":[{"net":"55","addr":"192.168.56.22","state":"可用","des":""},{},...]}
	 * @throws Exception
	 */
	String ListNetwork(String net)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject bku=new JSONObject();
			JSONArray netlist=new JSONArray();
			JSONArray iplist=new JSONArray();
			String[][] nets=dbd.readDB("vmm_network", "network", "id>0");
			if(!nets[0][0].equals("")) {
				for(int i=0;i<nets.length;i++)netlist.put(nets[i][0]);
				if(net.equals(""))net=nets[0][0];
				String[][] ips=dbd.readDB("vmm_ippool", "net,addr,state,des", "network='"+net+"' order by state, addr");
				if(!ips[0][0].equals("")) {
					for(int i=0;i<ips.length;i++) {
						JSONObject ip=new JSONObject();
						ip.put("net", ips[i][0]);
						ip.put("addr", ips[i][1]);
						ip.put("state", ips[i][2]);
						ip.put("des", ips[i][3]);
						iplist.put(i,ip);
					}
				}
			}
			bku.put("nets", netlist);
			bku.put("ips", iplist);
			bku.put("code", 200);
			return bku.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：删除指定网络，为避免删除网络后引发复杂的关联，暂不删除对应的IP池
	 * @param net	要删除的网络号
	 * @throws Exception 409，要删除的网络中还存在被使用的地址
	 */
	void DelNetwork(String net)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			int row=dbd.check("vmm_network", "network", net);
			if(row>0) {
				int row_ip=dbd.checknum("vmm_ippool", "id", "network='"+net+"' and state='已用' and des<>'网关'");
				if(row_ip>0)throw new Exception("[info]409,网络"+net+"还存在被使用的IP地址，请迁出后再删除。");
				else dbd.DelSQl("vmm_network", row, 1, 1);
			}
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：添加物理服务器
	 * @param servdat		服务器信息
	 * @throws Exception 404，IP所在网络不存在
	 * @throws Exception 409，存在重名或IP被使用
	 */
	void AddServ(String servdat)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject serv=new JSONObject(servdat);
//			判断是否已存在
			int row=dbd.check("vmm_phy_server", "hostname", serv.getString("hostname"));
			if(row>0)throw new Exception("[info]409,已经存在名为"+serv.getString("hostname")+"的服务器，不能重复添加。");
//			判断IP地址是否可用
			String ip=serv.getString("ipaddr");
			String net=ip.substring(0, ip.lastIndexOf("."))+".0";
			row=dbd.check("vmm_network", "network", net);
			if(row==0)throw new Exception("[info]404,不存在网络"+net+"下的地址，请确认修改。");
			String[][] ip_state=dbd.readDB("vmm_ippool", "state,id", "addr='"+ip+"'");
			if(ip_state[0][0].equals("已用"))throw new Exception("[info]409,ip "+ip+"已被使用，请查看IP池从新选择。");
			
//			添加服务器
			String[] colname= {"hostname","ipaddr","locate","sn","model","asset_sn","cpu","mem","disk","vm_num","cpu_used","cpu_rate_usage","mem_used",
					"mem_rate_usage","disk_used","disk_rate_usage","cpu_usable","disk_usable","mem_usable"};
			String[] record=new String[colname.length];
			for(int i=0;i<9;i++)record[i]=serv.getString(colname[i]);
			for(int i=9;i<16;i++)record[i]="0";
			for(int i=16;i<19;i++)record[i]="100";
			dbd.AppendSQl("vmm_phy_server", colname, record, 1, 1);
			
//			修改ip地址状态
			row=Integer.parseInt(ip_state[0][1]);
			dbd.UpdateSQl("vmm_ippool", row, "state", "已用");
			dbd.UpdateSQl("vmm_ippool", row, "des", record[0]);
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：获取物理服务器列表，携带服务器资源使用状态
	 * @return		JSON格式字符串，例如：
	 * {"domain":[{"name":"tech.cig.com","hosts":[{"hostname":"xxx","disk_used":"30","disk_usable":"70","mem_used":"20",	"mem_usable":"30"},{},...]},{},...]}
	 * @throws Exception
	 */
	String ListServ() throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject bku=new JSONObject();
			JSONArray domain=new JSONArray();
			JSONObject dos=new JSONObject();
			dos.put("name","tech.cig.com");
			JSONArray hosts=new JSONArray();
			
			String[][] servs=dbd.readDB("vmm_phy_server", "hostname,disk_used,disk_usable,mem_used,mem_usable", "id>0 order by hostname");
			if(!servs[0][0].equals("")) {
				for(int i=0;i<servs.length;i++) {
					JSONObject host=new JSONObject();
					host.put("hostname", servs[i][0]);
					host.put("disk_used", servs[i][1]);
					host.put("disk_usable", servs[i][2]);
					host.put("mem_used", servs[i][3]);
					host.put("mem_usable", servs[i][4]);
					hosts.put(i, host);
				}
			}
			dos.put("hosts", hosts);
			domain.put(dos);
			bku.put("domain", domain);
			bku.put("code", 200);
			return bku.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：获取物理服务器详细信息及其下属虚机列表
	 * @param hostname		要获取的服务器名
	 * @return		JSON格式字符串，例如：{"vmlist":[{"id":"1","name":"xx","os":"xx","cpu":"4","mem":"20",...},...],"hostname":"xxx",..}
	 * @throws Exception
	 */
	String GetServer(String hostname)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			判断是否存在
			int row=dbd.check("vmm_phy_server", "hostname", hostname);
			if(row==0)throw new Exception("[info]404,服务器"+hostname+"不存在，请确认。");
			
//			获取服务器信息
			String Colname="hostname,ipaddr,locate,sn,model,asset_sn,vm_num,cpu,mem,disk,cpu_used,cpu_rate_usage,mem_used,mem_rate_usage,disk_used,"
					+ "disk_rate_usage,cpu_usable,disk_usable,mem_usable";
			String[][] serv=dbd.readDB("vmm_phy_server", Colname, "id="+row);
			String[] keys=Colname.split(",");
			JSONObject bku=new JSONObject();
			for(int i=0;i<keys.length;i++)bku.put(keys[i], serv[0][i]);
			
//			获取虚拟机列表
			Colname="id,name,os,cpu,mem,disk,ip,des,user,type";
			String[] keys_vm= Colname.split(",");
			JSONArray vmlist=new JSONArray();
			String[][] vms=dbd.readDB("vmm_vm", Colname, "hostname='"+serv[0][0]+"'");
			if(!vms[0][0].equals("")) {
				for(int i=0;i<vms.length;i++) {
					JSONObject vm=new JSONObject();
					for(int j=0;j<keys_vm.length;j++)vm.put(keys_vm[j], vms[i][j]);
					vmlist.put(i, vm);
				}
			}
			bku.put("vmlist", vmlist);
			bku.put("code", 200);
			return bku.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：删除物理服务器，为避免删除服务器导致的虚拟机丢失，暂不进行对应虚拟机的删除
	 * @param hostname	要删除的主机名
	 * @throws Exception 
	 */
	void DelServ(String hostname)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			判断是否存在
			int row=dbd.check("vmm_phy_server", "hostname", hostname);
			if(row>0) {
				String[][] ip=dbd.readDB("vmm_phy_server", "ipaddr", "id="+row);
				dbd.DelSQl("vmm_phy_server", row, 1, 1);
				
//				修改ip地址状态，释放IP地址
				row=dbd.check("vmm_ippool", "addr", ip[0][0]); 
				dbd.UpdateSQl("vmm_ippool", row, "state", "可用");
				dbd.UpdateSQl("vmm_ippool", row, "des", "");
			}		
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：更新服务器信息
	 * @param oldname	要修改的服务器名称
	 * @param servdat		新的服务器信息数据
	 * @throws Exception 404，新的IP无效
	 * @throws Exception 409，服务器名已存在或IP已被使用
	 */
	void UpdateServ(String oldname,String servdat)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject serv=new JSONObject(servdat);
//			判断是否会重名
			String hostname=serv.getString("hostname");
			if(!hostname.equals(oldname)) {
				int row=dbd.check("vmm_phy_server", "hostname", hostname);
				if(row>0)throw new Exception("[info]409,服务器名称"+hostname+"已被使用。");  	
			}			
				
//			获取原有IP地址和服务器记录号
			String[][] oldip=dbd.readDB("vmm_phy_server", "ipaddr,id", "hostname='"+oldname+"'");
			int row=Integer.parseInt(oldip[0][1]);
			String tag="";
//			判断新IP地址是否可用
			String ip=serv.getString("ipaddr");	
			if(!oldip[0][0].equals(ip)) {
				String[][] ip_sta=dbd.readDB("vmm_ippool", "state,id", "addr='"+ip+"'");
				if(ip_sta[0][0].equals(""))throw new Exception("[info]404,新的IP地址"+ip+"无效。");
				if(ip_sta[0][0].equals("已用"))throw new Exception("[info]409,新的IP地址"+ip+"已被使用。");
				tag="y";
			}	
//			更新服务器基本信息
			String[] colname= {"hostname","ipaddr","locate","sn","model","asset_sn","cpu","mem","disk"};
			for(int i=0;i<colname.length;i++) {
				dbd.UpdateSQl("vmm_phy_server", row, colname[i], serv.getString(colname[i]));
			}	
//			更新虚拟机主机名
			String[][] vmids=dbd.readDB("vmm_vm", "id", "hostname='"+oldname+"'");
			if(!vmids[0][0].equals("")) {			
				for(int i=0;i<vmids.length;i++) {
					row=Integer.parseInt(vmids[i][0]);
					dbd.UpdateSQl("vmm_vm", row, "hostname", hostname);
				}
			}
//			如果新、旧IP不同，则进行IP状态变更
			if(!tag.equals("")) {
//				释放旧IP
				row=dbd.check("vmm_ippool", "addr", oldip[0][0]);
				if(row>0) {
					dbd.UpdateSQl("vmm_ippool", row, "state", "可用");
					dbd.UpdateSQl("vmm_ippool", row, "des", "");
				}				
//				更新新IP
				row=dbd.check("vmm_ippool", "addr", ip);
				if(row>0) {
					dbd.UpdateSQl("vmm_ippool", row, "state", "已用");
					dbd.UpdateSQl("vmm_ippool", row, "des",hostname );
				}				
			}
//			从新计算服务器资源状态
			server_res_math(hostname);
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：添加一台新虚拟机
	 * @param vmdat	新的虚拟机数据
	 * @throws Exception 404，服务器不存在或者IP地址无效
	 * @throws Exception 409，虚拟机名重复或者IP地址已被使用
	 */
	void AddVM(String vmdat)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject vm=new JSONObject(vmdat);
			String hostname=vm.getString("hostname");
//			判断主机是否存在
			int row=dbd.check("vmm_phy_server", "hostname", hostname);
			if(row==0)throw new Exception("[info]404,服务器"+hostname+"不存在。");		
//			判断虚拟机是否重名
			row=dbd.check("vmm_vm", "name", vm.getString("name"));
			if(row>0)throw new Exception("[info]409,已经存在名为"+vm.getString("name")+"的虚拟机。");		
			
//			判断IP是否有效
			String ip=vm.getString("ip");
			String[][] ip_state=dbd.readDB("vmm_ippool", "state,id", "addr='"+ip+"'");
			if(ip_state[0][0].equals(""))throw new Exception("[info]404,ip地址"+ip+"无效，请确认修改。");
			if(ip_state[0][0].equals("已用"))throw new Exception("[info]409,ip "+ip+"已被使用，请查看IP池从新选择。");
			
//			创建虚拟机记录
			String[] colname= {"hostname","name","os","cpu","mem","disk","ip","des","user","type"};
			String[] record=new String[colname.length];
			for(int i=0;i<colname.length;i++)record[i]=vm.getString(colname[i]);
			dbd.AppendSQl("vmm_vm", colname, record, 1, 1);
			
//			变更IP状态
			row=Integer.parseInt(ip_state[0][1]);
			dbd.UpdateSQl("vmm_ippool", row, "state", "已用");
			dbd.UpdateSQl("vmm_ippool", row, "des",record[1]);
			
//			从新计算主机的资源
			server_res_math(hostname);
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：删除指定的虚拟机并释放资源
	 * @param vm_name	要删除的虚拟机名
	 * @throws Exception
	 */
	void DelVM(String vm_name)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			判断是否存在
			int row=dbd.check("vmm_vm", "name", vm_name);
			if(row>0) {
//				删除虚拟机
				String[][] host=dbd.readDB("vmm_vm", "hostname,ip", "id="+row);
				dbd.DelSQl("vmm_vm", row, 1, 1);				
//				释放IP地址
				row=dbd.check("vmm_ippool", "addr", host[0][1]);
				if(row>0) {
					dbd.UpdateSQl("vmm_ippool", row, "state", "可用");
					dbd.UpdateSQl("vmm_ippool", row, "des", "");
				}
//				从新计算主机的资源
				server_res_math(host[0][0]);
			}
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：更新虚拟机信息
	 * @param oldname	虚拟机原有的名称，用来定位记录位置
	 * @param vmdat	新的虚拟机信息数据
	 * @throws Exception
	 */
	void UpdateVM(String oldname,String vmdat)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			获取原有数据
			String[][] vm_src=dbd.readDB("vmm_vm", "id,hostname,ip", "name='"+oldname+"'");
			if(vm_src[0][0].equals(""))throw new Exception("[info]409,虚拟机"+oldname+"不存在。");	
			
			JSONObject vm=new JSONObject(vmdat);
//			如果新旧名称不同，判断新名称是否存在
			String vm_name=vm.getString("name");
			if(!vm_name.equals(oldname)) {
				int row=dbd.check("vmm_vm", "name", vm_name);
				if(row>0)throw new Exception("[info]409,虚拟机"+vm_name+"已存在。");	
			}	
			
//			判断新IP是否有效
			String new_ip=vm.getString("ip");
			String tag="";
			if(!new_ip.equals(vm_src[0][2])) {
				String[][] ip_sta=dbd.readDB("vmm_ippool", "state,id", "addr='"+new_ip+"'");
				if(ip_sta[0][0].equals(""))throw new Exception("[info]404,新的IP地址"+new_ip+"无效。");
				if(ip_sta[0][0].equals("已用"))throw new Exception("[info]409,新的IP地址"+new_ip+"已被使用。");
				tag="y";
			}
//			更新虚拟机信息
			String[] colname= {"hostname","name","os","cpu","mem","disk","ip","des","user","type"};
			int row=Integer.parseInt(vm_src[0][0]);
			for(int i=0;i<colname.length;i++) {
				dbd.UpdateSQl("vmm_vm", row, colname[i], vm.getString(colname[i]));
			}
//			如果新、旧IP不同，则进行IP状态变更
			if(!tag.equals("")) {
//				释放旧IP
				row=dbd.check("vmm_ippool", "addr", vm_src[0][2]);
				if(row>0) {
					dbd.UpdateSQl("vmm_ippool", row, "state", "可用");
					dbd.UpdateSQl("vmm_ippool", row, "des", "");
				}				
//				更新新IP
				row=dbd.check("vmm_ippool", "addr", new_ip);
				if(row>0) {
					dbd.UpdateSQl("vmm_ippool", row, "state", "已用");
					dbd.UpdateSQl("vmm_ippool", row, "des",vm.getString("name"));
				}				
			}
			String hostname_new=vm.getString("hostname");
//			从新计算服务器资源状态
			server_res_math(hostname_new);
			if(!hostname_new.equals(vm_src[0][1]))server_res_math(vm_src[0][1]);
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：查找符合条件的虚拟机
	 * @param key	要查找的列名
	 * @param value	要查找的条件
	 * @return		JSON格式字符串，例如{"vms":[{"hostname":"","name":"","ip":"","os":"","cpu":"","mem":"","disk":"","des":"","user":"","type":""},{},...],"code":200}
	 * @throws Exception
	 */
	String ListVM(String key, String value)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			String colname="hostname,name,ip,os,cpu,mem,disk,des,user,type";
			String[][] vms=dbd.readDB("vmm_vm", colname, key+"='"+value+"'");
			JSONObject bku=new JSONObject();
			JSONArray vmlist=new JSONArray();
			if(!vms[0][0].equals("")) {
				String[] cols=colname.split(",");
				for(int i=0;i<vms.length;i++) {
					JSONObject vm=new JSONObject();
					for(int j=0;j<cols.length;j++)vm.put(cols[j], vms[i][j]);
					vmlist.put(i, vm);
				}
			}
			bku.put("code", 200);
			bku.put("vms", vmlist);
			return bku.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * 函数说明：计算服务器资源状态
	 * @param host		要计算的服务器主机名
	 * @throws Exception
	 */
	void server_res_math(String host)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			int row=dbd.check("vmm_phy_server", "hostname", host);
			if(row==0)throw new Exception("[info]404,服务器"+host+"不存在。");
			String[][] host_res=dbd.readDB("vmm_phy_server", "cpu,mem,disk", "id="+row);
			int vm_num=0;
			int cpu=Integer.parseInt(host_res[0][0]);
			int mem=Integer.parseInt(host_res[0][1]);
			int disk=Integer.parseInt(host_res[0][2]);
			int cpu_used=0;
			int cpu_usable=cpu;
			float cpu_rate_usage=0;
			int mem_used=0;
			int mem_usable=mem;
			float mem_rate_usage=0;
			int disk_used=0;
			int disk_usable=disk;
			float disk_rate_usage=0;

//			获取主机下的所有虚拟机资源占用情况
			String[][] vms=dbd.readDB("vmm_vm", "cpu,mem,disk", "hostname='"+host+"'");
			if(!vms[0][0].equals("")) {
				vm_num=vms.length;
				for(int i=0;i<vm_num;i++) {
					cpu_used=cpu_used+Integer.parseInt(vms[i][0]);
					mem_used=mem_used+Integer.parseInt(vms[i][1]);
					disk_used=disk_used+Integer.parseInt(vms[i][2]);
				}
				cpu_usable=cpu-cpu_used;
				cpu_rate_usage=(float)(cpu_used*10000/cpu)/100;
				mem_usable=mem-mem_used;
				mem_rate_usage=(float)(mem_used*10000/mem)/100;
				disk_usable=disk-disk_used;
				disk_rate_usage=(float)(disk_used*10000/disk)/100;
			}
			dbd.UpdateSQl("vmm_phy_server", row, "vm_num", ""+vm_num);
			dbd.UpdateSQl("vmm_phy_server", row, "cpu_used", ""+cpu_used);
			dbd.UpdateSQl("vmm_phy_server", row, "cpu_usable", ""+cpu_usable);
			dbd.UpdateSQl("vmm_phy_server", row, "cpu_rate_usage", ""+cpu_rate_usage);
			dbd.UpdateSQl("vmm_phy_server", row, "mem_used", ""+mem_used);
			dbd.UpdateSQl("vmm_phy_server", row, "mem_usable", ""+mem_usable);
			dbd.UpdateSQl("vmm_phy_server", row, "mem_rate_usage", ""+mem_rate_usage);
			dbd.UpdateSQl("vmm_phy_server", row, "disk_used", ""+disk_used);
			dbd.UpdateSQl("vmm_phy_server", row, "disk_usable", ""+disk_usable);
			dbd.UpdateSQl("vmm_phy_server", row, "disk_rate_usage", ""+disk_rate_usage);
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
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
