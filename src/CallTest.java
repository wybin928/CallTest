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
import java.util.concurrent.CountDownLatch;

public class CallTest implements Runnable {

	// URL for Vincio servlet
	public static String URLString = "http://192.168.235.128:8080/vincio/ProcessFlowServlet";
	// processflow alias configured in processflowConfig.xml
	public static String Processflow = "CCB";
	// Vincio ID
	public static String Username = "admin";;
	// Vincio password
	public static String Password = "admin";
	// processflow input parameter name
	public static String Input_parameter = "MESSAGE";
	// File path which stores input string
	public static String FileName = "D:/Work/Products/Vincio/3.8Performance/3.3vs3.8.3Batch/client/request.xml";
	// Execution Count for each thread
	public static int ExeCount = 10;
	// Used for concurrent testing
	public static int Threads = 5;
	// String read from file will input to Vincio
	public static String input;

	int threadNumber;
	private CountDownLatch countDownLatch;

	public CallTest(int count, CountDownLatch countDownLatch) {
		this.threadNumber = count;
		this.countDownLatch = countDownLatch;
	}

	public static long doGetTest(long ms, String input) throws IOException {
		input = URLEncoder.encode(input, "UTF-8"); // conver to URL coded
													// format, the %20.. stuff

		String parameter = "?_processflow_=" + Processflow + "&_username_="
				+ Username + "&_password_=" + Password + "&" + Input_parameter
				+ "=" + input;

		URL url = new URL(URLString + parameter);
		HttpURLConnection httpCon = null;
		InputStream vincioRspStr = null;

		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(false); // to do GET
			httpCon.setDoInput(true);
			httpCon.setUseCaches(true);

			long start = System.currentTimeMillis();

			vincioRspStr = httpCon.getInputStream(); // Send request to Vincio
														// servlet and read
														// response

			long tempms = System.currentTimeMillis() - start;
			ms = ms + tempms;
			InputStream buffer = new BufferedInputStream(vincioRspStr);
			Reader r = new InputStreamReader(buffer, "UTF-8");

			int c;
			StringBuffer out = new StringBuffer("");
			while ((c = r.read()) != -1) {
				out.append((char) c);
			}
			// 打印回应
			// System.out.println(out);
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
		String parameter = "_processflow_=" + Processflow + "&_username_="
				+ Username + "&_password_=" + Password + "&" + Input_parameter
				+ "=" + input;
		HttpURLConnection httpCon = null;
		OutputStreamWriter vincioReqStr = null;
		InputStream vincioRspStr = null;

		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true); // to do Post
			httpCon.setDoInput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setUseCaches(false);

			vincioReqStr = new OutputStreamWriter(httpCon.getOutputStream(),
					"UTF-8");
			vincioReqStr.write(parameter);
			vincioReqStr.flush();
			vincioReqStr.close(); // Need to close first to complete filling the
									// request
			long start = System.currentTimeMillis();

			vincioRspStr = httpCon.getInputStream(); // Send request to Vincio
														// servlet and read
														// response

			long tempms = System.currentTimeMillis() - start;
			ms = ms + tempms;

			InputStream buffer = new BufferedInputStream(vincioRspStr);
			Reader r = new InputStreamReader(buffer, "UTF-8");

			int c;
			StringBuffer out = new StringBuffer("");
			while ((c = r.read()) != -1) {
				out.append((char) c);
			}
			// 打印回应
			// System.out.println(out);
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

	public static String readFile(String FileName) {
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

	public void run() {

		for (int i = 1; i < ExeCount + 1; i++) {
			long ms = 0L;
			try {
				ms = doPostTest(ms, input);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("*****线程 [" + threadNumber + "]， 第 [" + i
					+ "] 次执行，耗时：" + ms + "ms");
		}
		
		// 线程倒数计数器减一
		countDownLatch.countDown();
	}

	public static void main(String[] args) throws Exception {

		input = readFile(FileName);
		
		long totalStart = System.currentTimeMillis();
		
		// 创建一个初始值为线程数的倒数计数器  
        CountDownLatch countDownLatch = new CountDownLatch(Threads);
		
		for (int threads = 0; threads < Threads; threads++) {
			Thread t = new Thread(new CallTest(threads + 1, countDownLatch ));
			t.start();
		}
		
		try  
        {  
            // 阻塞当前主线程，直到倒数计数器倒数到0  
            countDownLatch.await();  
        }  
        catch (InterruptedException e)  
        {  
            e.printStackTrace();  
        }  
		
		float totalDuration = (System.currentTimeMillis() - totalStart)/(float)1000;
		float tps = ExeCount*Threads/totalDuration;
		tps = (float) (Math.round(tps*100.0)/100.0);	//四舍五入保留后两位
		System.out.println("处理 [" + ExeCount*Threads + "] 条记录，用时" + totalDuration + "秒，折算 TPS ：" + tps );
	}

}
