Jordan Cattlelone, Jonathan Chang, Yuija Qiu

Design Document

For this app, we want to cleanly display two images as well as give the user a number of options to take or load an image as well as compare the images. Since having two images on one screen would be hard, we want to split the three images to their own tabs as well as having the take or load buttons for each image and also having one tab for the compare screen.

To accomplish this, each of the image tabs will have a ImageView and two buttons, one for taking an image and one for loading an image from the phone's gallery. The compare tab will have some TextView to display the results and one button to compare the images.

To maximize efficinecy and avoid freezing, we will run the take and load images as intents and run the label model on a background task. This allows the user to interact with the app while the model is running and will avoid our app being marked as crashed.