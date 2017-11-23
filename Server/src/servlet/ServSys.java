package servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import main.*;

/**
 * Servlet implementation class Server
 */
@WebServlet(description = "Rest server for asws service", urlPatterns = { "/servlet/ServSys" })
public class ServSys extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String logconf=System.getProperty("user.dir").replace("\\bin", "")+"\\conf\\ASWS\\asws_log.properties";
	Logger logger = Logger.getLogger(ServSys.class.getName());
	SysAPI api = new SysAPI();
	
    public ServSys() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String RespVau="";
		PropertyConfigurator.configure(logconf);
//		获取请求的API
		String ReqUrl=request.getRequestURI();
		logger.info("Receive API request: [GET]"+ReqUrl);
		int div=ReqUrl.lastIndexOf("/")+1;
		ReqUrl=ReqUrl.substring(div);
		
//		将API和参数发送到API接口函数处理
		try {		
			RespVau=api.DoAPI(ReqUrl);
		}catch(Exception e) {
			e.printStackTrace();
		}		
		
//		将API接口函数返回的结果发送给客户端
		response.setContentType("text/html;charset=UTF-8"); 
		PrintWriter out = response.getWriter();
		out.println(RespVau);
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		String body="";
		PropertyConfigurator.configure(logconf);
		
//		获取请求的API和附带参数		
		request.setCharacterEncoding("UTF-8"); 
		String ReqUrl=request.getRequestURI();
		String logtext="Receive API request: [POST]"+ReqUrl;
		int div=ReqUrl.lastIndexOf("/")+1;
		ReqUrl=ReqUrl.substring(div);		
		try {
			String readline="";	
			BufferedReader br=request.getReader();
		    while ((readline = br.readLine()) != null) {body += readline;}
		       br.close();
		 } catch (IOException e) {
			 logger.error("IOException: " + e);  
			 message="{\"code\"=500,\"message\"="+e+"}";
		 }								
		if(!body.equals(""))logtext=logtext+"++	[body]"+body;
		logger.info(logtext);
//		将API和参数发送到API接口函数处理	
		if(message.equals("")){
			try {
				message=api.DoAPI(ReqUrl);	
			}catch(Exception e) {
				e.printStackTrace();
			}		
		}		
//		将API接口函数返回的结果发送给客户端	
		response.setContentType("text/html;charset=UTF-8"); 
		PrintWriter out = response.getWriter();
		out.println(message);
		out.close();
	}
}
