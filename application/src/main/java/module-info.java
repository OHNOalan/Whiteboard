module application {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires kotlinx.coroutines.core.jvm;
    requires javafx.web;
    requires java.desktop;
    requires io.ktor.client.core;
    requires io.ktor.http;
    requires io.ktor.websockets;
    requires io.ktor.client.cio;
    requires io.ktor.utils;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;
    requires java.prefs;
    requires junit;
    exports net.codebot.application;
}