package me.wietlol.ioc.api

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class MissingServiceException(
	resolvingType: Class<*>,
	cause: Throwable? = null,
) : IocException(resolvingType, "Cannot resolve service of type '${resolvingType.name}'. No service was registered for this type.", cause)
