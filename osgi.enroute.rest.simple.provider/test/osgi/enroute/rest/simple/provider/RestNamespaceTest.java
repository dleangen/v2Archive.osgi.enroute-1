package osgi.enroute.rest.simple.provider;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.launchpad.Service;
import aQute.lib.io.IO;
import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;
import osgi.enroute.rest.api.RequireRestImplementation;

@RequireConfigurerExtender
@RequireRestImplementation
public class RestNamespaceTest {

	final static LaunchpadBuilder		builder = new LaunchpadBuilder().bndrun("bnd.bnd").debug();

	@Service
	BundleContext context;
	
	Launchpad lp;

	@Before
	public void setUp() throws Exception {
		lp = builder.create().inject(this);
	}

	@After
	public void tearDown() throws Exception {
		lp.close();
	}

	
    public static class RestExampleA implements REST {
        public String getA(RESTRequest rr, String arg) {
            return  "A::" + arg;
        }
        public String getX(RESTRequest rr, String arg) {
            return  "AX::" + arg;
        }
    }

    public static class RestExampleB implements REST {
        public String getB(RESTRequest rr, String arg) {
            return  "B::" + arg;
        }
        public String getX(RESTRequest rr, String arg) {
            return  "BX::" + arg;
        }
    }

    public static class RestExample implements REST {
        public String getDefault(RESTRequest rr, String arg) {
            return  "::" + arg;
        }
        
    }
    
    
    @Test
	public void testDifferentServletsDifferentNamespaces() throws Exception {
        ServiceRegistration<REST> deflt = regRest(new RestExample(), 0);
        ServiceRegistration<REST> a = regRest(new RestExampleA(), 0, "/A/*");
        ServiceRegistration<REST> b = regRest(new RestExampleB(), 1, "/B/*");

		try {
			assertRest( "A::arg1", "/A/a/arg1");
			assertRest( "AX::arg1", "/A/x/arg1");
			assertRest( "B::arg2", "/B/b/arg2");
			assertRest( "BX::arg2", "/B/x/arg2");
			assertRest( "::arg3", "/rest/default/arg3");
			
		} finally {
			deflt.unregister();
            a.unregister();
			b.unregister();
		}
	}

    private void assertRest(String expect, String path) throws IOException {
		URL url = new URL("http://localhost:8080"+path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			String s = IO.collect(conn.getInputStream());
			assertEquals("\""+expect+"\"", s);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (Exception e) {
			String s = IO.collect(conn.getErrorStream());
			System.out.println(s);
			throw e;
		}
	}

	public void testMergingEndpoints() throws Exception {
        ServiceRegistration<REST> deflt = regRest(new RestExample(), 0);
        ServiceRegistration<REST> a = regRest(new RestExampleA(), 1, "/shared/*");
        ServiceRegistration<REST> b = regRest(new RestExampleB(), 0, "/shared/*");

		try {
			assertRest( "A::arg1", "/shared/a/arg1");
			assertRest( "B::arg2", "/shared/b/arg2");
			assertRest( "::arg3", "/rest/default/arg3");
			
		} finally {
			deflt.unregister();
            a.unregister();
			b.unregister();
		}
    }

	public void testMultipleRegistrations() throws Exception {
        ServiceRegistration<REST> a = regRest(new RestExampleA(), 1, "/v1/*", "/v2/*", "/v3/*");
        ServiceRegistration<REST> b = regRest(new RestExampleB(), 0, "/v1/*");

		try {
			assertRest( "A::arg1", "/v1/a/arg1");
			assertRest( "B::arg2", "/v1/b/arg2");
			assertRest( "A::arg1", "/v2/a/arg1");
			assertRest( "A::arg1", "/v3/a/arg1");
			
		} finally {
            a.unregister();
			b.unregister();
		}
    }

	public void testRaningWithOverlapAFirst() throws Exception {
        ServiceRegistration<REST> a = regRest(new RestExampleA(), 1000);
        ServiceRegistration<REST> b = regRest(new RestExampleB(), 0);

		try {
			assertRest( "BX::arg1", "/rest/x/arg1");
		} finally {
            a.unregister();
			b.unregister();
		}
    }

	public void testRaningWithOverlapBFirst() throws Exception {
        ServiceRegistration<REST> a = regRest(new RestExampleA(), 0);
        ServiceRegistration<REST> b = regRest(new RestExampleB(), 1000);

		try {
			assertRest( "AX::arg1", "/rest/x/arg1");
		} finally {
            a.unregister();
			b.unregister();
		}
    }
	
    private ServiceRegistration<REST> regRest(REST instance, int ranking, String ... namespace) {
    	Hashtable<String, Object> dict = new Hashtable<>();
    	dict.put(Constants.SERVICE_RANKING, ranking);
    	if ( namespace.length != 0) {
    		dict.put( REST.ENDPOINT, namespace);
    	}
        return context.registerService(
                       REST.class,
                       instance,
                       dict
                       );
   }
}
