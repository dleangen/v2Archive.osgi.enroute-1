package osgi.enroute.web.simple.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.osgi.service.component.annotations.Component;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.launchpad.Service;
import aQute.lib.io.IO;
import junit.framework.TestCase;
import osgi.enroute.configurer.api.ConfigurationDone;

@SuppressWarnings("resource")
@Component
public class WebServerTest extends TestCase {

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

	@Service
	ConfigurationDone d;

	/**
	 * If only the hostname is provided (no slash), then the default action by the RedirectServlet
	 * will append "/index.html". This will serve the "debug" version of the WebServer INDEX page.
	 */
    public void testHostNameOnly() throws Exception {
        assertValid("enRoute INDEX", 200, "http://localhost:8080");
    }

    /**
     * If the request is for an empty path ("/" only), then the default action by the RedirectServlet
     * will append "index.html". This will serve the "debug" version of the WebServer INDEX page.
     */
	public void testEmptyRootPartition() throws Exception {
		assertValid("enRoute INDEX", 200, "http://localhost:8080/");
	}

	private void assertValid(String contents, int code, String uri) throws IOException {
        assertContents(contents, uri);
        assertStatusCode(code, uri);
	}

	private void assertContents(String content, String uri) throws IOException {
		URL url = new URL(uri);
		String s = IO.collect(url.openStream());
		assertEquals(content, s);
	}

	private void assertStatusCode(int code, String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
        assertEquals(code, openConnection.getResponseCode());
	}

}
