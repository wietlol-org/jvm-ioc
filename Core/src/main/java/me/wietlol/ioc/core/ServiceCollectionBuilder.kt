package me.wietlol.ioc.core

import me.wietlol.ioc.api.ServiceEntry
import me.wietlol.ioc.api.ServiceProvider
import me.wietlol.ioc.api.ServiceScope
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ServiceCollectionBuilder
{
	private val entries: MutableList<ServiceEntry<*>> = mutableListOf()
	
	inline fun <reified T : Any> factory(name: String? = null, noinline body: ServiceProvider.() -> T) =
		register(name, typeOf<T>(), ServiceScope.factory, body)
	
	inline fun <reified T : Any> single(name: String? = null, noinline body: ServiceProvider.() -> T) =
		register(name, typeOf<T>(), ServiceScope.single, body)
	
	fun register(name: String?, type: KType, scope: ServiceScope, factory: ServiceProvider.() -> Any) =
		register(name, type, scope.priority, factory)
	
	fun register(name: String?, type: KType, scope: Double, factory: ServiceProvider.() -> Any) =
		register(ServiceEntry(name, type, scope, factory))
	
	fun register(entry: ServiceEntry<*>)
	{
		entries.add(entry)
	}
	
	fun register(provider: ServiceProvider)
	{
		provider.entries.forEach { register(it) }
	}
	
	fun build(): ServiceProvider =
		StringServiceCollection(entries)
}

fun serviceCollection(builder: ServiceCollectionBuilder.() -> Unit): ServiceProvider =
	ServiceCollectionBuilder()
		.apply(builder)
		.build()
