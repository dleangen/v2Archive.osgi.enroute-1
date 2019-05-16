package osgi.enroute.bostock.d3.webresource.capabilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.osgi.annotation.bundle.Requirement;

import osgi.enroute.namespace.WebResourceNamespace;

/**
 * A sample web resource requirement 
 */

@Requirement(namespace = WebResourceNamespace.NS, filter = "(&(" + WebResourceNamespace.NS + "="
		+ D3Constants.D3PATH + ")(version>=" + D3Constants.D3VERSION
		+ "))")
@Retention(RetentionPolicy.CLASS)
public @interface RequireD3Webresource {

	String[] resource() default "d3.js";
	int priority() default 0;
}
