package g.sw

import g.sw.star.http.Dispatcher
import g.sw.star.http.StarHttpServer
import g.sw.star.ssh.StarSshServer

object Star {
    @JvmStatic
    fun main(args: Array<String>) {
        val dispatcher = Dispatcher()
        val httpPort = args.firstOrNull()?.toIntOrNull() ?: 8080
        val sshPort = args.getOrNull(1)?.toIntOrNull() ?: 2222

        StarHttpServer(httpPort, dispatcher).start()
        StarSshServer(sshPort, dispatcher).start()

        println("Star shining.")
        Thread.sleep(Long.MAX_VALUE)
    }
}
