package osgi.enroute.web.simple.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.lib.io.IO;
import aQute.libg.map.MAP;
import junit.framework.TestCase;
import osgi.enroute.configurer.api.ConfigurationDone;
import osgi.enroute.servlet.api.ConditionalServlet;

@SuppressWarnings("resource")
@Component
public class DispatcherTest extends TestCase {

	static LaunchpadBuilder	builder;
	
	static {
		try {
			builder = new LaunchpadBuilder().bndrun("bnd.bnd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Launchpad lp;
	
	public void setUp() throws Exception {
		lp = builder.create().inject(this);
	}
	@Override
	protected void tearDown() throws Exception {
		lp.close();		
	}

	public void testAccess() throws Exception {
		URL url = new URL("http://localhost:8080/");
		String s = IO.collect(url.openStream());
		System.out.println(s);
	}

	StringBuffer		buffer = new StringBuffer();
	
	public class TestSkipServlet implements ConditionalServlet {
		private String id;

		public TestSkipServlet(String id) {
			this.id = id;
		}

		@Override

		public boolean doConditionalService(HttpServletRequest rq, HttpServletResponse rsp) throws Exception {
			buffer.append(id).append(".");
			return false;
		}

	}
	
	public void testSkippingServlets() throws Exception {
		ServiceRegistration<ConditionalServlet> r10 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestSkipServlet("10"), MAP.$(Constants.SERVICE_RANKING, 10).asHashtable());
		ServiceRegistration<ConditionalServlet> r20 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestSkipServlet("20"), MAP.$(Constants.SERVICE_RANKING, 20).asHashtable());
		ServiceRegistration<ConditionalServlet> r30 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestSkipServlet("30"), MAP.$(Constants.SERVICE_RANKING, 30).asHashtable());
		ServiceRegistration<ConditionalServlet> r40 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestSkipServlet("40"), MAP.$(Constants.SERVICE_RANKING, 40).asHashtable());
		ServiceRegistration<ConditionalServlet> r50 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestServlet("50"), MAP.$(Constants.SERVICE_RANKING, 50).asHashtable());

		try {
			assertContents( "50:/foo", "http://localhost:8080/foo");
			
			// Check calling order
			assertEquals("10.20.30.40.", buffer.toString());
			
			// Check if the normal resource read works
			assertContents( "TEST - BAR", "http://localhost:8080/foo/bar/test.txt");
		} finally {
			r10.unregister();
			r20.unregister();
			r30.unregister();
			r40.unregister();
			r50.unregister();
		}
	}

	private void assertContents(String content, String uri) throws IOException {
		URL url = new URL(uri);
		String s = IO.collect(url.openStream());
		assertEquals(content, s);
	}

	public class TestServlet implements ConditionalServlet {
		private String id;

		public TestServlet(String id) {
			this.id = id;
		}

		@Override

		public boolean doConditionalService(HttpServletRequest rq, HttpServletResponse rsp) throws Exception {
			String path = rq.getServletPath();
			if ( path != null && path.equals("/foo")) {
				rsp.getWriter().print(id + ":" + path);
				return true;
			}
			return false;
		}

	}

	public void testRankingServlets() throws Exception {
		ServiceRegistration<ConditionalServlet> r100 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestServlet("100"), MAP.$(Constants.SERVICE_RANKING, 100).asHashtable());
		ServiceRegistration<ConditionalServlet> r50 = lp.getBundleContext().registerService(ConditionalServlet.class,
				new TestServlet("50"), MAP.$(Constants.SERVICE_RANKING, 50).asHashtable());

		try {
			URL url = new URL("http://localhost:8080/foo");
			String s = IO.collect(url.openStream());
			assertEquals("50:/foo", s);
		} finally {
			r100.unregister();
			r50.unregister();
		}
	}

	boolean beBad = false;
	public class BadServlet implements ConditionalServlet {

		@Override
		public boolean doConditionalService(HttpServletRequest rq, HttpServletResponse rsp) throws Exception {
			if (!beBad) {
				rsp.getWriter().print(rq.getServletPath());
				return true;
			}
			else {
				throw new IllegalArgumentException();
			}
		}

	}

	public void testBadServlet() throws Exception {
		ServiceRegistration<ConditionalServlet> blacklist = lp.getBundleContext().registerService(ConditionalServlet.class,
				new BadServlet(), MAP.$(Constants.SERVICE_RANKING, 100).asHashtable());

		try {
			beBad = false;
			URL url = new URL("http://localhost:8080/foo");
			String s = IO.collect(url.openStream());
			assertEquals("/foo", s);

			beBad = true;
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			assertEquals(404, openConnection.getResponseCode());

			Thread.sleep(1000);
			
			beBad = false;
			openConnection = (HttpURLConnection) url.openConnection();
			s = IO.collect(url.openStream());
			assertEquals("/foo", s);
		} finally {
			blacklist.unregister();
		}
	}

	@Reference
	void setConfigurationDone( ConfigurationDone d) {
		System.out.println("DispatcherTest - Configuration Done");
	}
}
