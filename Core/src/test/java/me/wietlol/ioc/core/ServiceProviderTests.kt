package me.wietlol.ioc.core

import me.wietlol.ioc.api.*
import org.junit.Test
import kotlin.reflect.typeOf

class ServiceProviderTests : LocalTestModule()
{
	@Test
	fun `assert that an empty service collection, returns null for it's optional service resolving`() = unitTest {
		val provider = serviceCollection { }
		
		val getByType = provider.get<String?>()
		val getByName = provider.get<String?>("test")
		val getByDecorator: String? by provider
		
		assertThat(getByType).isNull()
		assertThat(getByName).isNull()
		assertThat(getByDecorator).isNull()
	}
	
	@Test
	fun `assert that an empty service collection, throws exceptions for it's mandatory service resolving`() = unitTest {
		val provider = serviceCollection { }
		
		assertThrows<MissingServiceException> { provider.get<String>() }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
		
		assertThrows<MissingServiceException> { provider.get<String>("test") }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
		
		// value is not resolved until used
		val value: String by provider
		@Suppress("UNUSED_EXPRESSION")
		assertThrows<MissingServiceException> { value }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
	}
	
	@Test
	fun `assert that named entries, cannot be resolved by unnamed lookup`() = unitTest {
		val provider = serviceCollection {
			single("name") { "test" }
		}
		
		assertThrows<MissingServiceException> { provider.get<String>() }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
	}
	
	@Test
	fun `assert that unnamed entries, cannot be resolved by named lookup`() = unitTest {
		val provider = serviceCollection {
			single { "test" }
		}
		
		assertThrows<MissingServiceException> { provider.get<String>("name") }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
	}
	
	@Test
	fun `assert that named entries, can be resolved by different types`() = unitTest {
		val provider = serviceCollection {
			single("name") { "test" }
			single("name") { 42 }
		}
		
		assertThat(provider.get<String>("name"))
			.isEqualTo("test")
		assertThat(provider.get<Int>("name"))
			.isEqualTo(42)
	}
	
	@Test
	fun `assert that entries are type-safe`() = unitTest {
		val provider = serviceCollection {
			register(null, typeOf<String>(), ServiceScope.single) { 0 }
		}

		assertThrows<IllegalServiceTypeException> { provider.get<String>() }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. Actual service was of type 'java.lang.Integer'.")
	}
	
	@Test
	fun `assert that entries cannot be resolved by subtypes`() = unitTest {
		val provider = serviceCollection {
			single { "test" }
		}

		assertThrows<MissingServiceException> { provider.get<CharSequence>() }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.CharSequence'. No service was registered for this type.")
	}
	
	@Test
	fun `assert that entries cannot be resolved by supertypes`() = unitTest {
		val provider = serviceCollection {
			single<CharSequence> { "test" }
		}

		assertThrows<MissingServiceException> { provider.get<String>() }
			.property { ::message }
			.isEqualTo("Cannot resolve service of type 'java.lang.String'. No service was registered for this type.")
	}
	
	@Test
	fun `assert that when a factory throws an exception, the actual exception has information of the service resolving`() = unitTest {
		val provider = serviceCollection {
			single<String> { throw Exception("test") }
		}

		assertThrows<IllegalServiceFactoryException> { provider.get<String>() }
			.apply {
				property { ::message }
					.isEqualTo("Cannot resolve service of type 'java.lang.String'. Factory method threw an unhandled exception.")
			}
			.apply {
				property { ::cause }
					.isNotNull()
					.isInstanceOf<Exception>()
					.property { ::message }
					.isEqualTo("test")
			}
			
	}
	
	@Test
	fun `assert that named service collections, resolve named entries without named lookup`() = unitTest {
		val provider = serviceCollection {
			single("name") { "test" }
		}

		val named = provider.named("name")
		
		assertThat(provider.get<String?>())
			.isNull()
		
		assertThat(named.get<String>())
			.isEqualTo("test")
	}
	
	@Test
	fun `assert that factory services are recreated every time they are resolved`() = unitTest {
		var counter = 0
		val provider = serviceCollection {
			factory { counter++; "test" }
		}
		
		assertThat(counter).isEqualTo(0)
		provider.get<String?>()
		assertThat(counter).isEqualTo(1)
		provider.get<String?>()
		assertThat(counter).isEqualTo(2)
		provider.get<String?>()
		assertThat(counter).isEqualTo(3)
	}
	
	@Test
	fun `assert that single services are recreated every time they are resolved`() = unitTest {
		var counter = 0
		val provider = serviceCollection {
			single { counter++; "test" }
		}
		
		assertThat(counter).isEqualTo(0)
		provider.get<String?>()
		assertThat(counter).isEqualTo(1)
		provider.get<String?>()
		assertThat(counter).isEqualTo(1)
		provider.get<String?>()
		assertThat(counter).isEqualTo(1)
	}
	
	@Test
	fun `assert that registering an existing service provider, copies over all entries into the new service provider`() = unitTest {
		val initial = serviceCollection {
			single { "test" }
		}
		val provider = serviceCollection {
			register(initial)
		}
		
		assertThat(provider.get<String>())
			.isEqualTo("test")
	}
	
	@Test
	fun `assert that registering an existing named service provider, copies over the correctly named entries into the new service provider`() = unitTest {
		val initial = serviceCollection {
			single("name") { "test" }
			single("name") { 42 }
		}
		val provider = serviceCollection {
			register(initial.named("name"))
		}
		
		assertThat(provider.get<String>())
			.isEqualTo("test")
		assertThat(provider.get<Int>())
			.isEqualTo(42)
	}
	
	@Test
	fun `assert that named service providers, can resolve named services by decoration`() = unitTest {
		val provider = serviceCollection {
			single("name") { "test" }
		}
		val value: String by provider.named("name")
		
		assertThat(value)
			.isEqualTo("test")
	}
	
	@Test
	fun `assert that named service providers, can be sub-names`() = unitTest {
		val provider = serviceCollection {
			single("services-name") { "test" }
		}
		val named = provider.named("services-")
		
		assertThat(named.get<String>("name"))
			.isEqualTo("test")
	}
	
	@Test
	fun `assert that named service providers, can create a named service provider, providing their sub-named entries`() = unitTest {
		val provider = serviceCollection {
			single("services-name") { "test" }
		}
		val named = provider.named("services-").named("name")
		
		assertThat(named.get<String>())
			.isEqualTo("test")
	}
	
	@Test
	fun `assert that when multiple services are registered, the last service is the service that is resolved`() = unitTest {
		val provider = serviceCollection {
			single { "test1" }
			single { "test2" }
		}
		
		assertThat(provider.get<String>())
			.isEqualTo("test2")
	}
	
	@Test
	fun `assert that when multiple services are registered, all services can be resolved`() = unitTest {
		val provider = serviceCollection {
			single { "test1" }
			single { "test2" }
		}
		
		assertThat(provider.getAll<String>().toList())
			.isEqualTo(listOf("test1", "test2"))
	}
	
	@Test
	fun `assert that when multiple services are registered, all services can be resolved when named`() = unitTest {
		val provider = serviceCollection {
			single("name") { "test1" }
			single("name") { "test2" }
		}
		
		val named = provider.named("name")
		
		assertThat(named.getAll<String>().toList())
			.isEqualTo(listOf("test1", "test2"))
	}
}
