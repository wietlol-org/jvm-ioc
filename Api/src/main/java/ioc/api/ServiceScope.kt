package ioc.api

enum class ServiceScope(
	val priority: Double
)
{
	single(0.0),
	factory(1.0),
}
