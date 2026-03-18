import java.net.URL
val reviewsJson = URL("https://api.jikan.moe/v4/anime/52991/reviews").readText()
println(reviewsJson.take(1000))

val fullJson = URL("https://api.jikan.moe/v4/anime/52991/full").readText()
println(fullJson.take(1000))