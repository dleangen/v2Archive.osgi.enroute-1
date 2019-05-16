package osgi.enroute.iot.admin.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.implementation.ImplementationNamespace;

/**
 * Require an implementation for the this specification
 */
@Requirement(namespace = ImplementationNamespace.IMPLEMENTATION_NAMESPACE, filter = "(&("
		+ ImplementationNamespace.IMPLEMENTATION_NAMESPACE + "="
		+ IotAdminConstants.IOT_ADMIN_SPECIFICATION_NAME + ")${frange;${version;==;"
		+ IotAdminConstants.IOT_ADMIN_SPECIFICATION_VERSION + "}})")
@Retention(RetentionPolicy.CLASS)
public @interface RequireIotAdminImplementation {}
