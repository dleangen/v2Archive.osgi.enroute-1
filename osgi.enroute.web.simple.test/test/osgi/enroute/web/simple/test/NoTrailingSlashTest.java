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
public class NoTrailingSlashTest extends TestCase {

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
	ConfigurationDone cd;
	
    /**
     * If only the BSN is provided on the path (no slash), then the result should be a 404.
     * The BFS requires an exact path match.
     */
    public void _ignore_testBFSRootNoSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/bnd/osgi.enroute.web.simple.test");
    }

    /**
     * If only the BSN is provided on the path (with slash), then the result should be a 404.
     * The BFS requires an exact path match.
     */
    public void _ignore_testBFSRootWithSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/bnd/osgi.enroute.web.simple.test/");
    }

    public void _ignore_testBFSFolderNoSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/bnd/osgi.enroute.web.simple.test/foo");
    }

    public void _ignore_testBFSFolderWithSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/bnd/osgi.enroute.web.simple.test/foo/");
    }

    /**
     * If only the BSN is provided on the path (no slash), then the result should be a 404.
     * The BFS requires an exact path match.
     */
    public void _ignore_testMixinRootNoSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/osgi.enroute.web.simple.test");
    }

    /**
     * If only the BSN is provided on the path (with slash), then the result should be a 200
     * because the redirect servlet will have intervened. This is the one case that is different
     * from the BFS.
     */
    public void testMixinRootWithSlash() throws Exception {
        assertValid("TEST - BND - TOP", 200, "http://localhost:8080/osgi.enroute.web.simple.test/");
    }

    public void _ignore_testMixinFolderNoSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/osgi.enroute.web.simple.test/foo");
    }

    public void _ignore_testMixinFolderWithSlash() throws Exception {
        assertStatusCode(404, "http://localhost:8080/osgi.enroute.web.simple.test/foo/");
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
