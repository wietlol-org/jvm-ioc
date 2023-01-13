package ioc.api

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class IllegalServiceTypeException(
	resolvingType: Class<*>,
	val serviceType: Class<*>,
	cause: Throwable? = null,
) : IocException(resolvingType, "Cannot resolve service of type '${resolvingType.name}'. Actual service was of type '${serviceType.name}'.", cause)
