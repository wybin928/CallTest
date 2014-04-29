import java.io.BufferedReader;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class CallTest37And38 {

	public static final String URLString = "http://192.168.235.128:8080/vincio/ProcessFlowServlet";	// URL for Vincio servlet
	public static final String Processflow = "HW"; 	// processflow alias configured in processflowConfig.xml
	public static final String Username = "admin"; 	// Vincio ID
	public static final String Password = "admin"; 	// Vincio password
	public static final String Input_parameter = "INPUT";	// processflow input parameter name
	public static final String FileName = "D:/Work/Products/Vincio/3.8Performance/3.3vs3.8.3Batch/client/request.xml";	// File path which stores input string
	public static final int ExeCount = 1; 			// Execution Count
	public static final int Threads = 1;			// Used for concurrent testing

	public static long doGetTest(long ms, String input) throws IOException {
		input = URLEncoder.encode(input, "UTF-8");	// conver to URL coded format, the %20.. stuff
		
		String parameter = "?_processflow_=" + Processflow
							+ "&_username_=" + Username 
							+ "&_password_=" + Password 
							+ "&" + Input_parameter	+ "=" + input;
		
		URL url = new URL(URLString + parameter);
		HttpURLConnection httpCon = null;
		InputStream vincioRspStr = null;

		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(false); // to do GET
			httpCon.setDoInput(true);
			httpCon.setUseCaches(true);

			long start = System.currentTimeMillis();

			vincioRspStr = httpCon.getInputStream(); // Send request to Vincio servlet and read response

			long tempms = System.currentTimeMillis() - start;
			ms = ms + tempms;
			System.out.println("lasting:" + tempms + "ms");
			InputStream buffer = new BufferedInputStream(vincioRspStr);
			Reader r = new InputStreamReader(buffer, "UTF-8");

			int c;
			StringBuffer out = new StringBuffer("");
			while ((c = r.read()) != -1) {
				out.append((char) c);
			}
			// 打印回应
			System.out.println(out);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (vincioRspStr != null) {
				vincioRspStr.close();
			}
			httpCon.disconnect();
		}

		return ms;
	}

	public static long doPostTest(long ms, String input) throws IOException {
		
		URL url = new URL(URLString);
		String parameter = "_processflow_=" + Processflow
							+ "&_username_=" + Username
							+ "&_password_=" + Password
							+ "&" + Input_parameter	+ "=" + input;
		HttpURLConnection httpCon = null;
		OutputStreamWriter vincioReqStr = null;
		InputStream vincioRspStr = null;

		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true); // to do Post
			httpCon.setDoInput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setUseCaches(false);

			vincioReqStr = new OutputStreamWriter (httpCon.getOutputStream(), "UTF-8");
			vincioReqStr.write(parameter);
			vincioReqStr.flush();
			vincioReqStr.close();	// Need to close first to complete filling the request
			long start = System.currentTimeMillis();

			vincioRspStr = httpCon.getInputStream(); // Send request to Vincio servlet and read response

			long tempms = System.currentTimeMillis() - start;
			ms = ms + tempms;
			System.out.println("lasting:" + tempms + "ms");
			InputStream buffer = new BufferedInputStream(vincioRspStr);
			Reader r = new InputStreamReader(buffer, "UTF-8");

			int c;
			StringBuffer out = new StringBuffer("");
			while ((c = r.read()) != -1) {
				out.append((char) c);
			}
			// 打印回应
			System.out.println(out);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (vincioRspStr != null) {
				vincioRspStr.close();
			}
			httpCon.disconnect();
		}

		return ms;

	}

	public static String readFile (String FileName) {
		String input = null;
		// Read request message
		StringBuffer content = new StringBuffer("");
		File myFile = new File(FileName);
		if (!myFile.exists()) {
			System.err.println("Can't Find " + FileName);
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(myFile));
			String str;
			while ((str = in.readLine()) != null) {
				content.append(str);
			}
			in.close();
		} catch (IOException e) {
			e.getStackTrace();
		}

		input = new String(content.toString());
		return input;
	}
	
	public static void main(String[] args) throws Exception {
		long ms = 0L;

		String input=readFile(FileName);

		for (int i = 0; i < ExeCount; i++) {
			int j = i + 1;
			System.out.println("*****************第【" + j
					+ "】次执行。****************");
			ms = doPostTest(ms, input);
		}
		System.out.println("总共耗时：" + ms + "ms");
	}

}
