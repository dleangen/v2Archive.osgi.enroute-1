package osgi.enroute.capabilities;

import org.osgi.annotation.bundle.Requirement;

/**
 * The purpose of this capability is to be never satisfied. This can be required
 * by bundles that are compile only or class path only.
 */
@Requirement(namespace = "osgi.unresolvable", filter = "(&(must.not.resolve=*)(!(must.not.resolve=*)))")
public @interface Unresolvable {

}
