package de.mosimtech.common.jpa.converter.jpa.namespace

import de.mosimtech.common.core.namespace.Namespace


enum class TestNamespace(name: String): Namespace {
    Test("test");

    override val identifier = name
}
