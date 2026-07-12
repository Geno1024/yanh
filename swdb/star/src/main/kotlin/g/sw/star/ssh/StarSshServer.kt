package g.sw.star.ssh

import g.sw.star.http.Dispatcher
import org.apache.sshd.common.keyprovider.KeyPairProvider
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.command.CommandFactory
import org.apache.sshd.server.shell.ShellFactory
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPairGenerator

class StarSshServer(
    private val port: Int = 2222,
    private val dispatcher: Dispatcher = Dispatcher(),
) {
    private val server = SshServer.setUpDefaultServer()

    fun start() {
        server.port = port
        server.host = "0.0.0.0"
        server.keyPairProvider = KeyPairProvider { listOf(kp()) }
        server.passwordAuthenticator = PasswordAuthenticator { username, password, _ ->
            username == "star" && password == "star"
        }
        server.shellFactory = ShellFactory { StarShell(dispatcher) }
        server.commandFactory = CommandFactory { _, cmd -> StarCommand(cmd, dispatcher) }
        server.start()
        println("SSH server listening on port $port")
    }

    fun stop() {
        server.stop(true)
    }

    private fun kp() = KeyPairGenerator.getInstance("RSA").run {
        initialize(2048)
        generateKeyPair()
    }
}

private class StarShell(private val dispatcher: Dispatcher) : Command {
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private var error: OutputStream? = null
    private var exitCallback: ExitCallback? = null

    override fun setInputStream(input: InputStream?) { this.input = input }
    override fun setOutputStream(output: OutputStream?) { this.output = output }
    override fun setErrorStream(error: OutputStream?) { this.error = error }
    override fun setExitCallback(callback: ExitCallback?) { this.exitCallback = callback }

    override fun start(channel: ChannelSession?, env: Environment?) {
        Thread {
            try {
                val reader = input!!.bufferedReader()
                val writer = output!!.bufferedWriter()

                writer.write("Star Family Server\r\n")
                writer.flush()

                while (true) {
                    writer.write("star> ")
                    writer.flush()
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    when (line.trim().lowercase()) {
                        "exit", "quit" -> break
                        "help" -> {
                            writer.write("Commands: {class} {method} [--key value ...]\r\n")
                            writer.write("  help, exit/quit\r\n")
                            writer.flush()
                        }
                        else -> {
                            try {
                                val parsed = CommandParser.parse(line)
                                val resolved = dispatcher.resolveClass(parsed.className)
                                val result = dispatcher.dispatch(resolved, parsed.methodName, parsed.args)
                                writer.write((result?.toString() ?: "ok") + "\r\n")
                            } catch (e: Exception) {
                                writer.write("Error: ${e.cause?.message ?: e.message}\r\n")
                            }
                            writer.flush()
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                exitCallback?.onExit(0)
            }
        }.apply { isDaemon = true }.start()
    }

    override fun destroy(channel: ChannelSession?) {}
}

private class StarCommand(
    private val cmdLine: String,
    private val dispatcher: Dispatcher,
) : Command {
    private var output: OutputStream? = null
    private var exitCallback: ExitCallback? = null

    override fun setInputStream(input: InputStream?) {}
    override fun setOutputStream(output: OutputStream?) { this.output = output }
    override fun setErrorStream(error: OutputStream?) {}
    override fun setExitCallback(callback: ExitCallback?) { this.exitCallback = callback }

    override fun start(channel: ChannelSession?, env: Environment?) {
        try {
            val text = when (cmdLine.trim().lowercase()) {
                "help" -> "Commands: {class} {method} [--key value ...]\n"
                "exit", "quit" -> ""
                else -> {
                    val parsed = CommandParser.parse(cmdLine)
                    val resolved = dispatcher.resolveClass(parsed.className)
                    val result = dispatcher.dispatch(resolved, parsed.methodName, parsed.args)
                    (result?.toString() ?: "ok") + "\n"
                }
            }
            if (text.isNotEmpty()) {
                output!!.write(text.toByteArray())
                output!!.flush()
            }
        } catch (e: Exception) {
            val text = "Error: ${e.cause?.message ?: e.message}\n"
            output!!.write(text.toByteArray())
            output!!.flush()
        } finally {
            exitCallback?.onExit(0)
        }
    }

    override fun destroy(channel: ChannelSession?) {}
}
