package me.wietlol.ioc.core

import me.wietlol.ioc.api.IllegalServiceFactoryException
import me.wietlol.ioc.api.IllegalServiceTypeException
import me.wietlol.ioc.api.MissingServiceException
import me.wietlol.ioc.api.ServiceEntry
import me.wietlol.ioc.api.ServiceProvider
import java.util.TreeMap
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

class StringServiceCollection(
	override val entries: List<ServiceEntry<*>>
) : ServiceProvider
{
	val registry: Map<String, ServiceEntry<*>> = entries.associateBy { key(it) }
	private val caches = TreeMap<Double, MutableMap<String, Any?>>().descendingMap()
	
	override fun named(name: String): ServiceProvider =
		PrefixedStringServiceCollection(name, this)
	
	override fun <T> get(type: KType): T =
		getServiceByType(null, type)
	
	override fun <T> get(name: String, type: KType): T =
		getServiceByType(name, type)
	
	override fun <T> getValue(instance: Any?, property: KProperty<*>): T =
		getServiceByType(null, property.returnType)
	
	@Throws(IllegalServiceFactoryException::class, IllegalServiceTypeException::class, MissingServiceException::class)
	private fun <T> getServiceByType(name: String?, type: KType): T
	{
		val resolvingType = type.jvmErasure.java
		val key = key(name, resolvingType)
		
		val cached = caches.entries
			.map { it.value[key] }
			.firstOrNull()
		
		@Suppress("UNCHECKED_CAST")
		if (cached != null)
			return cached as T
		
		val entry = registry[key]
		
		val service = getValue<T>(entry, type, resolvingType)
		
		if (entry != null && entry.scope < 1.0)
			caches.computeIfAbsent(entry.scope) { hashMapOf() }[key] = service
		
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
