# BitSliceViewer
A simple gui to display 24 bit image bit-planes

## Screenshots
### Photograph
![photograph](Screenshots/photograph.gif)
### Artwork
![artwork](Screenshots/artwork.gif)
## Usage
Requires the latest Java version.

This program is designed for 24 bit-per-pixel bitmap images.
To convert an image to 24bpp bitmap using imagemagick, use the following command:
> convert myimage.xxx -type truecolor myimage.bmp
To open an image, pass it as a command-line argument when running the JAR file:
> java -jar BitsliceViewer.jar /Path/to/myimage.bmp
***File-picker support coming soon!***
