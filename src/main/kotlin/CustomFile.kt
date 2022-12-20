import java.io.Serializable

class CustomFile(
    var name: String,
    var content: ByteArray,
    var dest: String,
    var uploadTime: String,
) : Serializable {

    companion object{
        private const val serialVersionUID = 1L
    }

}