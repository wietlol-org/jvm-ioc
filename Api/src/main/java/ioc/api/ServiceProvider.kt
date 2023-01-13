package ioc.api

import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

interface ServiceProvider
{
	val entries: List<ServiceEntry<*>>
	
	fun named(name: String): ServiceProvider
	
	fun <T> get(type: KType): T
	
	fun <T> get(name: String, type: KType): T
	
	fun <T : Any> getAll(type: KType): Sequence<T>
	
	operator fun <T> getValue(instance: Any?, property: KProperty<*>): T
	
	/*protected*/ fun <T> getValue(entry: ServiceEntry<*>?, type: KType, resolvingType: Class<*>): T
	{
		if (entry == null)
		{
			if (type.isMarkedNullable)
				@Suppress("UNCHECKED_CAST")
				return null as T
			throw MissingServiceException(resolvingType)
		}
		
		val service = try
		{
			entry.factory(this)
		}
		catch (ex: Throwable)
		{
			throw IllegalServiceFactoryException(resolvingType, ex)
		}
		
		if (type.jvmErasure.isInstance(service).not())
			throw IllegalServiceTypeException(resolvingType, service.javaClass)
		
		@Suppress("UNCHECKED_CAST")
		return service as T
	}
}

inline fun <reified T> ServiceProvider.get(): T =
	get(typeOf<T>())

inline fun <reified T> ServiceProvider.get(name: String): T =
	get(name, typeOf<T>())

inline fun <reified T : Any> ServiceProvider.getAll(): Sequence<T> =
	getAll(typeOf<T>())
