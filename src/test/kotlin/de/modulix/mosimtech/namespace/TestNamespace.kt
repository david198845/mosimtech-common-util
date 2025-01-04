package de.modulix.mosimtech.namespace

import de.modulix.mosimtech.database.namespace.Namespace


enum class TestNamespace(name: String): Namespace {
    Test("test");

    override val identifier = name
}