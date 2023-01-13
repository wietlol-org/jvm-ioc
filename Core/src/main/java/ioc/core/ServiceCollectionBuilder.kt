package ioc.core

import ioc.api.ServiceEntry
import ioc.api.ServiceProvider
import ioc.api.ServiceScope
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
		provider.entries.forEach {
			register(it.proxiedTo(provider))
		}
	}
	
	fun build(): ServiceProvider =
		StringServiceCollection(entries)
}

private fun <T : Any> ServiceEntry<T>.proxiedTo(provider: ServiceProvider) =
	proxied {
		provider.getValue(this@proxiedTo, type, type.javaClass)
	}

private fun <T : Any> ServiceEntry<T>.proxied(factory: ServiceProvider.() -> T) =
	ServiceEntry(
		name,
		type,
		scope,
		factory
	)

fun serviceCollection(builder: ServiceCollectionBuilder.() -> Unit): ServiceProvider =
	ServiceCollectionBuilder()
		.apply(builder)
		.build()
