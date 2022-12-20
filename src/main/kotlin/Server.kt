import java.io.*
import java.net.ConnectException
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


class Server(private val ip: String, private val port: Int) : Thread() {

    private val DEST_RESULT_PATH = "files/results/$port"

    private var socket: Socket? = null
    private var path: Path? = null
    private var fileToSend: CustomFile? = null
    private var resultFile: CustomFile? = null
    private var bf: BufferedOutputStream? = null

    fun connect(): Boolean {
        try {
            socket = Socket(ip, port)
            socket?.let { s ->
                bf = BufferedOutputStream(socket!!.getOutputStream())
                return true
            }
        } catch (e: ConnectException) {
            e.printStackTrace()
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

        try {
            println("Waiting return from the server")

            do {

                val objectAsByte = ByteArray(socket!!.receiveBufferSize)
                val bf = BufferedInputStream(socket!!.getInputStream())
                bf.read(objectAsByte)

                resultFile = getObjectFromByte(objectAsByte) as CustomFile?

            } while (resultFile == null)

            resultFile?.let {

                val dir = DEST_RESULT_PATH + File.separator + it.name;
                val tempFile = File(DEST_RESULT_PATH)
                if(!tempFile.exists()){
                    tempFile.mkdirs()
                }

                println("Writing file at $dir")

                val fos = FileOutputStream(dir)
                fos.write(it.content)
                fos.close()
                println("All file was successfully written")

                val finalFile = Files.readAllLines(Path.of(dir))
                println("One server count $finalFile occurrence in the file")



            }

        }catch (e: Exception){
            println("Something went wrong")
            e.printStackTrace()
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