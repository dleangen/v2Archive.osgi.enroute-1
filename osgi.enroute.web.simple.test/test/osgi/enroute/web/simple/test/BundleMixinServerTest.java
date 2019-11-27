package osgi.enroute.web.simple.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.lib.io.IO;
import junit.framework.TestCase;
import osgi.enroute.configurer.api.ConfigurationDone;

@SuppressWarnings("resource")
@Component
public class BundleMixinServerTest extends TestCase {

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

	/**
	 * This will serve the "debug" version of the WebServer INDEX page.
	 */
    public void testBundleMixinServer() throws Exception {
        assertValid("enRoute INDEX", 200, "http://localhost:8080/index.html");
    }

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

    public void testFolderNoSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/foo");
    }

    public void testFolderWithSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/foo/");
    }

    public void test404() throws Exception {
        assertStatusCode(404, "http://localhost:8080/foo/NoSuchFile");
    }

    // Test backwards compatibility by accessing segregated content as mixin content

    /**
     * BFS = BundleFileServer
     */
    public void testBundleFileServer() throws Exception {
        assertValid("TEST - BND - TOP", 200, "http://localhost:8080/osgi.enroute.web.simple.test/index.html");
    }

    public void testBFSRootNoSlash() throws Exception {
        assertValid("TEST - BND - TOP", 200, "http://localhost:8080/osgi.enroute.web.simple.test/");
    }

    /**
     * If only the BSN is provided on the path (with slash), then the result should be a 200
     * because the redirect servlet will have intervened. This is the one case that is different
     * from the BFS.
     */
    public void testBFSRootWithSlash() throws Exception {
        assertValid("TEST - BND - TOP", 200, "http://localhost:8080/osgi.enroute.web.simple.test/");
    }

    public void testBFSFolderNoSlash() throws Exception {
        assertValid("TEST - FOO - INDEX", 200, "http://localhost:8080/osgi.enroute.web.simple.test/foo");
    }

    public void testBFSFolderWithSlash() throws Exception {
        assertValid("TEST - FOO - INDEX", 200, "http://localhost:8080/osgi.enroute.web.simple.test/foo/");
    }

    public void testBFS404() throws Exception {
        assertStatusCode(404, "http://localhost:8080/osgi.enroute.web.simple.test/foo/NoSuchFile");
    }

    public void testBFSGetFile() throws Exception {
        assertValid("TEST - BAR", 200, "http://localhost:8080/osgi.enroute.web.simple.test/foo/bar/test.txt");
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

	@Reference
	void setConfigurationDone( ConfigurationDone d) {
		System.out.println("BFSTest - Configuration Done");
	}
}
