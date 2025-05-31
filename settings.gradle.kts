plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "common-util"


include("common-util-jpa")
include("common-util-core")
include("common-util-r2dbc")
include("common-util-mongo")