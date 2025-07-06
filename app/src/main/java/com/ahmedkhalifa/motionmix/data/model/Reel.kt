package com.ahmedkhalifa.motionmix.data.model


data class Reel(
    val id: String,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val description: String,
    val author: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean = false
)

fun getSampleVideos(): List<Reel> {
    return listOf(
        Reel(
            id = "1",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4",
            description = "Sample 5s video from SampleLib",
            author = "SampleLib",
            likesCount = 1234,
            commentsCount = 56,
            sharesCount = 12,
            isLiked = true
        ),
        Reel(
            id = "2",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://samplelib.com/lib/preview/mp4/sample-10s.mp4",
            description = "Sample 10s video from SampleLib",
            author = "SampleLib",
            likesCount = 876,
            commentsCount = 32,
            sharesCount = 8
        ),
        Reel(
            id = "3",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4",
            description = "Big Buck Bunny short clip",
            author = "Test Videos",
            likesCount = 2100,
            commentsCount = 101,
            sharesCount = 25
        ),
        Reel(
            id = "4",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
            description = "Sample video from FileSamples",
            author = "FileSamples",
            likesCount = 1500,
            commentsCount = 70,
            sharesCount = 18
        ),
        Reel(
            id = "5",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://samplelib.com/lib/preview/mp4/sample-15s.mp4",
            description = "Sample 15s video from SampleLib",
            author = "SampleLib",
            likesCount = 950,
            commentsCount = 45,
            sharesCount = 10
        )
    )
}
