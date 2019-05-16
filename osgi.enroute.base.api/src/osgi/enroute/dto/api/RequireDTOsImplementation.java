package osgi.enroute.dto.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.implementation.ImplementationNamespace;

/**
 * Require an implementation for the this specification
 */
@Requirement(namespace = ImplementationNamespace.IMPLEMENTATION_NAMESPACE, filter = "(&("
		+ ImplementationNamespace.IMPLEMENTATION_NAMESPACE + "="
		+ DTOsConstants.DTOS_SPECIFICATION_NAME + ")${frange;${version;==;"
		+ DTOsConstants.DTOS_SPECIFICATION_VERSION + "}})")
@Retention(RetentionPolicy.CLASS)
public @interface RequireDTOsImplementation {}
