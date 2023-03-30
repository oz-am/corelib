package com.inflexit.core.webclient

public const val DEFAULT_PORT: Int = 0
public data class URLProtocol(val name: String, val defaultPort: Int) {
    init {
        require(name.all { it.isLowerCase() }) { "All characters should be lower case" }
    }

    @Suppress("PublicApiImplicitType")
    public companion object {
        /**
         * HTTP with port 80
         */
        public val HTTP: URLProtocol = URLProtocol("http", 80)

        /**
         * secure HTTPS with port 443
         */
        public val HTTPS: URLProtocol = URLProtocol("https", 443)

        /**
         * Web socket over HTTP on port 80
         */
        public val WS: URLProtocol = URLProtocol("ws", 80)

        /**
         * Web socket over secure HTTPS on port 443
         */
        public val WSS: URLProtocol = URLProtocol("wss", 443)

        /**
         * Socks proxy url protocol.
         */
        public val SOCKS: URLProtocol = URLProtocol("socks", 1080)

        /**
         * Protocols by names map
         */
        public val byName: Map<String, URLProtocol> = listOf(HTTP, HTTPS, WS, WSS, SOCKS).associateBy { it.name }

        /**
         * Create an instance by [name] or use already existing instance
         */
        public fun createOrDefault(name: String): URLProtocol = name.toLowerCasePreservingASCIIRules().let {
            byName[it] ?: URLProtocol(it, DEFAULT_PORT)
        }
    }
}

/**
 * Check if the protocol is websocket
 */
public fun URLProtocol.isWebsocket(): Boolean = name == "ws" || name == "wss"

/**
 * Check if the protocol is secure
 */
public fun URLProtocol.isSecure(): Boolean = name == "https" || name == "wss"


private fun toLowerCasePreservingASCII(ch: Char): Char = when (ch) {
    in 'A'..'Z' -> ch + 32
    in '\u0000'..'\u007f' -> ch
    else -> ch.lowercaseChar()
}

private fun toUpperCasePreservingASCII(ch: Char): Char = when (ch) {
    in 'a'..'z' -> ch - 32
    in '\u0000'..'\u007f' -> ch
    else -> ch.lowercaseChar()
}

public fun String.toLowerCasePreservingASCIIRules(): String {
    val firstIndex = indexOfFirst {
        toLowerCasePreservingASCII(it) != it
    }

    if (firstIndex == -1) {
        return this
    }

    val original = this
    return buildString(length) {
        append(original, 0, firstIndex)

        for (index in firstIndex..original.lastIndex) {
            append(toLowerCasePreservingASCII(original[index]))
        }
    }
}