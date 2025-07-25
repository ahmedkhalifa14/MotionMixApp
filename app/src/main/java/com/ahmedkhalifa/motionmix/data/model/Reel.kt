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
            mediaUrl = "https://firebasestorage.googleapis.com/v0/b/videoplayerapp-64f8a.appspot.com/o/v4.MOV?alt=media&token=39aff3c1-a323-412c-9383-be5dfed2e1fb",
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
            mediaUrl = "https://firebasestorage.googleapis.com/v0/b/videoplayerapp-64f8a.appspot.com/o/videos%2F%2451%2F1751848247249.mp4?alt=media&token=434f81d2-44af-4e74-b3f4-ede0b5887436",
            description = "Sample 10s video from SampleLib",
            author = "SampleLib",
            likesCount = 876,
            commentsCount = 32,
            sharesCount = 8
        ),
        Reel(
            id = "3",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://firebasestorage.googleapis.com/v0/b/videoplayerapp-64f8a.appspot.com/o/v5.MOV?alt=media&token=11b83134-e6dd-44f0-a6f2-96392790b8a0",
            description = "Big Buck Bunny short clip",
            author = "Test Videos",
            likesCount = 2100,
            commentsCount = 101,
            sharesCount = 25
        ),
        Reel(
            id = "4",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://firebasestorage.googleapis.com/v0/b/videoplayerapp-64f8a.appspot.com/o/app_demo.mp4?alt=media&token=ad8219f5-8440-4e33-8b8a-687c5bb33326",
            description = "Sample video from FileSamples",
            author = "FileSamples",
            likesCount = 1500,
            commentsCount = 70,
            sharesCount = 18
        ),
        Reel(
            id = "5",
            thumbnailUrl = "https://images.pexels.com/photos/1108562/pexels-photo-1108562.jpeg",
            mediaUrl = "https://firebasestorage.googleapis.com/v0/b/videoplayerapp-64f8a.appspot.com/o/videos%2F%2451%2F1751848247249.mp4?alt=media&token=434f81d2-44af-4e74-b3f4-ede0b5887436",
            description = "Sample 15s video from SampleLib",
            author = "SampleLib",
            likesCount = 950,
            commentsCount = 45,
            sharesCount = 10
        )
    )
}
