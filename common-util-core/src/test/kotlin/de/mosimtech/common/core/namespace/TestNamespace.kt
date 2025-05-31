package de.mosimtech.common.core.namespace



enum class TestNamespace(name: String): Namespace {
    Test("test");

    override val identifier = name
}
