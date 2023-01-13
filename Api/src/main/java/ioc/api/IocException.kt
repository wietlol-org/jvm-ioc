package ioc.api

abstract class IocException(
	val resolvingType: Class<*>,
	message: String,
	cause: Throwable? = null,
) : Exception(message, cause)
