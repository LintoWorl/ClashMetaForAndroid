package app.hw.network.util

import android.net.SSLCertificateSocketFactory
import android.util.Log
import com.github.kr328.clash.common.log.Logger.TAG_HTTP
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class SecureSSLSocketFactory(
    private val conn: HttpsURLConnection,
    private val factory: SSLSocketFactory
) : SSLSocketFactory() {

    override fun createSocket(socket: Socket, host: String, port: Int, b: Boolean): Socket {
        return enableSNI(factory.createSocket(socket, host, port, b), host)
    }

    override fun createSocket(host: String, port: Int): Socket {
        return enableSNI(factory.createSocket(host, port), host)
    }

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {
        return enableSNI(factory.createSocket(host, port, localHost, localPort), host)
    }

    override fun createSocket(host: InetAddress, port: Int): Socket {
        return enableSNI(factory.createSocket(host, port), host.hostName)
    }

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket {
        return enableSNI(
            factory.createSocket(address, port, localAddress, localPort),
            address.hostName
        )
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return factory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return factory.supportedCipherSuites
    }

    private fun enableSNI(socket: Socket, host: String): Socket {
        val sslSocket = socket as SSLSocket
        try {
            var peerHost = conn.getRequestProperty("Host")
            if (peerHost == null) {
                peerHost = host
            }
            Log.i(TAG_HTTP, "customized createSocket. host: $peerHost")
            // create and connect SSL socket, but don't do hostname/certificate verification yet
            val sslSocketFactory =
                SSLCertificateSocketFactory.getDefault(0) as SSLCertificateSocketFactory

            // set up SNI before the handshake
            Log.i(TAG_HTTP, "Setting SNI hostname")
            sslSocketFactory.setHostname(sslSocket, peerHost)
            sslSocket.startHandshake()
        } catch (e: IOException) {
            Log.e(TAG_HTTP, "exception at enableSNI:${e.message}")
        }
        return sslSocket
    }
}