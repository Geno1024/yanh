package g.buildsrc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CountTask(@get:Input key: String) : DefaultTask()
{
    init
    {
        group = "count"
        outputs.upToDateWhen { false }
    }

    @OutputFile
    val file: File = project.file("$key-count.txt")

    @Internal
    val count: Int = file.takeIf(File::exists)?.readText(Charsets.UTF_8)?.trim()?.toIntOrNull() ?: 0

    @TaskAction
    fun action()
    {
        if (file.exists())
        {
            val renew = file.readText(Charsets.UTF_8).trim().toIntOrNull()?.inc() ?: 1
            file.writeText(renew.toString())
        }
        else
        {
            file.writeText("0")
        }
    }

    abstract class RunCountTask : CountTask("run")

    abstract class PackCountTask : CountTask("pack")
}
