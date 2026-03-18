import java.net.URL
val json = URL("https://api.jikan.moe/v4/anime/1").readText()
println(json.take(500))