# Custom-Camera

In this I have used Camera Api version for opening camera in the fragment.

In Camera Fragment I have open the camera through thread so app open fastly.

In this fragment, Calculation is done for getting the best preview of camera by creating Custom Surface View in which preview of image can be seen . 

When the take picture button is clicked, the callback goes to onPictureTaken method of Camera PictureCallback Interface. 
Inside this method the byteArray of data is sent to the Preview Fragment insdie the bundle data .

In the Preview Fragment I have taken the byte array from bundle ,then converted into the square bitmap image .For  making the image square Matrix class are used and calculation for making the image square and saving in the pictures folder in which i have created the folder of app, in which saved image will be through another AysncTask.
