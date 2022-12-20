import java.io.*
import java.net.ConnectException
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class CustomServer {

    class Control{
        companion object {

            @Synchronized
            fun updateOccurrence(value: Int, dir: String){
                println("-----> UPDATING VALUE IN FILE")
                val f = File(dir)
                val fw = FileWriter(f)
                val bw = BufferedWriter(fw)
                bw.write(value.toString())
                bw.close()
                fw.close()
            }

        }
    }

    class Server(private val ip: String, private val port: Int) : Thread() {

        //val control = Control()
        val serverAddress = "$ip:$port"

        companion object{
            private const val DEST_RESULT_PATH = "files/results"
        }

        private var socket: Socket? = null
        private var path: Path? = null
        private var fileToSend: CustomFile? = null
        private var resultFile: CustomFile? = null
        private var bf: BufferedOutputStream? = null

        fun connect(): Boolean {
            try {
                socket = Socket(ip, port)
                socket?.let { s ->
                    bf = BufferedOutputStream(s.getOutputStream())
                    println("-----> Connected to $ip:$port")
                    return true
                }
            } catch (e: ConnectException) {
                //e.printStackTrace()
                println("-----> Not possible connect to $ip:$port")
            }
            return false;
        }

        fun sendZipFile(path: Path) {
            this.path = path
            var fis: FileInputStream? = null
            try {
                val f: File = File(path.toUri())
                val bFile = ByteArray(f.length().toInt())
                fis = FileInputStream(f)
                fis.read(bFile)
                fis.close()

                fileToSend = CustomFile(
                    f.name,
                    bFile,
                    "",
                    Date().toString()
                )

                sendFileToServer()

            } catch (e: Exception) {
                e.printStackTrace()
                println("Something went wrong")
            }
        }

        private fun sendFileToServer() {
            val bytea: ByteArray? = serializeFile()
            bytea?.let {
                println("-----> SENDING FILE ${fileToSend?.name} TO SERVER")
                bf!!.write(it)
                bf!!.flush()
                //bf!!.close()
            }

            //socket!!.close()
        }

        private fun serializeFile(): ByteArray? {
            try {
                val bao = ByteArrayOutputStream()
                val ous: ObjectOutputStream = ObjectOutputStream(bao)
                ous.writeObject(fileToSend)
                return bao.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun run() {

            /*try {




            }catch (e: Exception){
                println("Something went wrong")
                e.printStackTrace()
            }*/

            println("-----> Waiting return from the server")

            do {

                val objectAsByte = ByteArray(socket!!.receiveBufferSize)
                val bf = BufferedInputStream(socket!!.getInputStream())
                bf.read(objectAsByte)

                resultFile = getObjectFromByte(objectAsByte) as CustomFile?

            } while (resultFile?.content == null)

            resultFile?.let { it ->

                val dir = DEST_RESULT_PATH + File.separator + it.name;
                val tempFile = File(DEST_RESULT_PATH)
                var lastResult: Int = 0

                if(!tempFile.exists()){
                    tempFile.mkdirs()
                }else{
                    val result = Files.readAllLines(Path.of(dir))
                    result.forEach {
                        println("LAST RESULT: " + it.toInt())
                        lastResult = it.toInt()
                    }
                }

                println("-----> Writing file at $dir")

                val fos = FileOutputStream(dir)
                fos.write(it.content)
                fos.close()
                println("-----> All file was successfully written")

                val finalFile = Files.readAllLines(Path.of(dir))
                if(finalFile.isNotEmpty()){
                    println("-----> One server count $finalFile occurrence in the file")
                    finalFile.forEach { f ->
                        if(f.isNotEmpty()){
                            val currentResult = f.toInt()
                            val total = lastResult + currentResult
                            //val newFos = FileOutputStream(dir)
                            Control.updateOccurrence(total, dir)
                        }
                    }
                }else{
                    println("-----> One server can't count occurrence in the file")
                }
            }

        }

        private fun getObjectFromByte(objectAsByte: ByteArray): Any? {
            var obj: Any? = null
            var bis: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            try {
                bis = ByteArrayInputStream(objectAsByte)
                ois = ObjectInputStream(bis)
                obj = ois.readObject()
                bis.close()
                ois.close()
            } catch (e: IOException) {
                println("Something went wrong with the file")
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                println("Class not found")
                e.printStackTrace()
            }
            return obj
        }



    }

}