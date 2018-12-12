Team 1009 Group Project App Analysis

While running the app to take pictures with the camera and from the gallery, the app runs well and does not
tend to use a lot of resources. CPU hardly rises above 10%, battery consumption is very small, and memory usage does
not rise above 40MB. After the image starts to be processed, CPU rises to 40% and memory usage rises to 250MB. We use 
the profiler on Android Studio to measure these statistics, and let the profiler run for roughly 30 seconds while taking 
and selecting pictures before moving on to image processing with the model. It took about 20 seconds to fully process
both of the images. The results are shown in the tables in the video included in this submission.