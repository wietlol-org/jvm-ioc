package ioc.api

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class IllegalServiceFactoryException(
	resolvingType: Class<*>,
	cause: Throwable?,
) : IocException(resolvingType, "Cannot resolve service of type '${resolvingType.name}'. Factory method threw an unhandled exception.", cause)
