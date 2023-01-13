package ioc.core

import ioc.api.IllegalServiceFactoryException
import ioc.api.IllegalServiceTypeException
import ioc.api.MissingServiceException
import ioc.api.ServiceEntry
import ioc.api.ServiceProvider
import java.util.TreeMap
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

class StringServiceCollection(
	override val entries: List<ServiceEntry<*>>
) : ServiceProvider
{
	val registry: Map<String, List<ServiceEntry<*>>> = entries.groupBy { key(it) }
	private val caches = TreeMap<Double, MutableMap<ServiceEntry<*>, Any?>>().descendingMap()
	
	override fun named(name: String): ServiceProvider =
		PrefixedStringServiceCollection(name, this)
	
	override fun <T> get(type: KType): T =
		getServiceByType(null, type)
	
	override fun <T> get(name: String, type: KType): T =
		getServiceByType(name, type)
	
	override fun <T : Any> getAll(type: KType): Sequence<T> =
		getAllServicesByType(null, type)
	
	override fun <T> getValue(instance: Any?, property: KProperty<*>): T =
		getServiceByType(null, property.returnType)
	
	@Throws(IllegalServiceFactoryException::class, IllegalServiceTypeException::class, MissingServiceException::class)
	private fun <T> getServiceByType(name: String?, type: KType): T
	{
		val resolvingType = type.jvmErasure.java
		val key = key(name, resolvingType)
		
		val entries = registry[key] ?: emptyList()
		val entry = entries.lastOrNull()
		
		return getValue(entry, type, resolvingType)
	}
	
	@Throws(IllegalServiceFactoryException::class, IllegalServiceTypeException::class, MissingServiceException::class)
	internal fun <T> getAllServicesByType(name: String?, type: KType, filter: (ServiceEntry<*>) -> Boolean = {true}): Sequence<T>
	{
		val resolvingType = type.jvmErasure.java
		val key = key(name, resolvingType)
		
		val entries = registry[key] ?: emptyList()
		
		return entries
			.asSequence()
			.withIndex()
			.filter { filter(it.value) }
			.map { getValue(it.value, type, resolvingType) }
	}
	
	override fun <T> getValue(entry: ServiceEntry<*>?, type: KType, resolvingType: Class<*>): T
	{
		if (entry != null)
		{
			val cached = caches.entries
				.map { it.value[entry] }
				.firstOrNull()
			
			@Suppress("UNCHECKED_CAST")
			if (cached != null)
				return cached as T
		}
		
		val service = super.getValue<T>(entry, type, resolvingType)
		
		if (entry != null && entry.scope < 1.0)
			caches.computeIfAbsent(entry.scope) { hashMapOf() }[entry] = service
		
		return service
	}
	
	private fun key(entry: ServiceEntry<*>) =
		key(entry.name, entry.type.jvmErasure.java)
	
	private fun key(name: String?, type: Class<*>) =
		if (name == null)
			type.name
		else
			"$name:${type.name}"
}
