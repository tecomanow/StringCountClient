import java.nio.file.Files
import java.nio.file.Path


fun  main(args: Array<String>) {

    val serversIps = "files/servers.txt"
    val filesPath = "files/files.txt"
    //val zipFilesPaths = arrayListOf("files/teste.zip", "files/teste1.zip", "files/teste2.zip")
    val zipFilesPaths = mutableListOf<String>()
    val connectedServers : ArrayList<CustomServer.Server> = ArrayList()

    Files.readAllLines(Path.of(filesPath)).forEach { path ->
        zipFilesPaths.add(path)
    }

    Files.readAllLines(Path.of(serversIps)).forEach { serverAddress ->
        println(serverAddress)
        val serverAndPort = serverAddress.split(":")
        val server = serverAndPort[0].trim()
        val port = serverAndPort[1].trim()

        val s = CustomServer.Server(server, port.toInt())
        if (s.connect()) {
            connectedServers.add(s)
        }

    }

    println("The connections was accepted by: ${connectedServers.size} servers")

    if(connectedServers.size > 0){
        var count = 0
        connectedServers.forEach {
            it.sendZipFile(Path.of(zipFilesPaths[count]))
            //it.setListOccurrences(occurrences)
            it.start()
            count++
        }
    }

    /*if(connectedServers.size > 0 && zipFilesList.size > 0){
        var count = 0;
        connectedServers.forEach {
            val path = zipFilesList[count]
            it.sendZipFile(Path.of(path))
            it.start()
        }
    }*/

}