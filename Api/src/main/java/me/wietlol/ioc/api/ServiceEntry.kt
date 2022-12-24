package me.wietlol.ioc.api

import kotlin.reflect.KType

class ServiceEntry<T : Any>(
	val name: String?,
	val type: KType,
	val scope: Double,
	val factory: ServiceProvider.() -> T
)
