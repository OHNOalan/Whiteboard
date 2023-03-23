module application {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires kotlinx.coroutines.core.jvm;
    requires shared;
    requires java.desktop;
    requires io.ktor.client.core;
    requires io.ktor.http;
    requires io.ktor.websockets;
    requires io.ktor.client.cio;
    requires io.ktor.utils;
    exports net.codebot.application;
}