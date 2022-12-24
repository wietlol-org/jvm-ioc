package me.wietlol.ioc.core

import me.wietlol.ioc.api.ServiceEntry
import me.wietlol.ioc.api.ServiceProvider
import kotlin.reflect.KProperty
import kotlin.reflect.KType

class PrefixedStringServiceCollection(
	val prefix: String,
	val provider: StringServiceCollection,
) : ServiceProvider
{
	override val entries: List<ServiceEntry<*>>
		get() = provider.entries
			.filter { it.name?.startsWith(prefix) == true }
			.map { ServiceEntry(it.name!!.removePrefix(prefix).takeIf { it.isNotEmpty() }, it.type, it.scope, it.factory) }
	
	override fun named(name: String): ServiceProvider =
		PrefixedStringServiceCollection("$prefix$name", provider)
	
	override fun <T> get(type: KType): T =
		provider.get(prefix, type)
	
	override fun <T> get(name: String, type: KType): T =
		provider.get("$prefix$name", type)
	
	override fun <T> getValue(instance: Any?, property: KProperty<*>): T =
		provider.get(prefix, property.returnType)
}
