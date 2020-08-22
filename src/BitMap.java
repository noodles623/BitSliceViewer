import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitMap {
	// Header Sizes
	final int BMP_LEN = 14;
	final int DIB_LEN = 40;
	final int HEADER_LEN = 54;
	private int paddingLen;
	
	// Addresses and size
	final int ID_ADDR = 0x00;
	final int ID_LEN = 2;
	
	final int SIZE_ADDR = 0x22;
	final int SIZE_LEN = 4;
	
	final int WIDTH_ADDR = 0x12;
	final int WIDTH_LEN = 4;
	
	final int HEIGHT_ADDR = 0x16;
	final int HEIGHT_LEN = 4;
	
	final int BPP_ADDR = 0x1c;
	final int BP_LEN = 2;
	
	final int PIXEL_START_LOCATION = 0xa;
	final int PIXEL_START_LEN = 4;
	private int pixelStartAddress;
	
	// Definitions
	final int _24BPP = 0x18;
	
	// File Information
	private int pixelSize;
	private int imageWidth;
	private int imageHeight;
	
	// Byte Arrays of image info
	private int[] bmpHeader = new int[BMP_LEN];
	private int[] dibHeader = new int[DIB_LEN];
	private int[] pixelData;
	private int[] headerPadding;
	
	private void loadHeaders(String imgLocation) {
		try {
			InputStream file = new FileInputStream(imgLocation);
			BufferedInputStream imgStream = new BufferedInputStream(file);
			for(int i = 0; i < BMP_LEN; i++)
				bmpHeader[i] = imgStream.read();
			for(int i = 0; i < DIB_LEN; i++)
				dibHeader[i] = imgStream.read();
			file.close();
			imgStream.close();
		} catch (IOException e) { e.printStackTrace(); };
	}
	
	private void loadPadding(String imgLocation) {
		try {
		InputStream file = new FileInputStream(imgLocation);
		BufferedInputStream imgStream = new BufferedInputStream(file);
		paddingLen = pixelStartAddress - HEADER_LEN;
		headerPadding = new int[paddingLen];
		
		int addr = 0;
		for(int i = 0; i < HEADER_LEN; i++) {
			imgStream.read();
			addr++;
		}
		for(int i = 0; i < paddingLen; i++)
			headerPadding[i] = imgStream.read();
		file.close();
		imgStream.close();
		} catch (IOException e) { e.printStackTrace(); };	
	}
	
	public BitMap(String imgLocation) {
		loadHeaders(imgLocation);
		assert isBitmap();
		assert is24bpp();
		pixelSize = readPixelSize();
		imageWidth = readImageWidth();
		imageHeight = readImageHeight();
		pixelData = new int[pixelSize];
		pixelStartAddress = readPixelAddr();
		loadPadding(imgLocation);		
		loadPixelData(imgLocation);
	}
	
	private boolean isBitmap() {
		return ((char)bmpHeader[ID_ADDR] == 'B' &&
				(char)bmpHeader[ID_ADDR+1] == 'M');
	}
	
	private boolean is24bpp() {
		return dibHeader[BPP_ADDR-14] == _24BPP;
	}
	
	private int readPixelSize() {
		int size= 0x00000000;
		for(int i = 0; i < SIZE_LEN; i++)
			size += (dibHeader[SIZE_ADDR-14 + i] << 8*i);
		return size;
	}
	private int readPixelAddr() {
		int addr= 0x00000000;
		for(int i = 0; i < PIXEL_START_LEN; i++)
			addr += (bmpHeader[PIXEL_START_LOCATION + i] << 8*i);
		return addr;
	}
	
	private int readImageWidth() {
		int width= 0x00000000;
		for(int i = 0; i < WIDTH_LEN; i++)
			width += (dibHeader[WIDTH_ADDR-14 + i] << 8*i);
		return width;
	}
	
	private int readImageHeight() {
		int width= 0x00000000;
		for(int i = 0; i < HEIGHT_LEN; i++)
			width += (dibHeader[HEIGHT_ADDR-14 + i] << 8*i);
		return width;
	}
	
	private void loadPixelData(String imgLocation) {
		int curPoint = 0;
		try { 
			InputStream file = new FileInputStream(imgLocation);
			BufferedInputStream pixStream = new BufferedInputStream(file);
			while(curPoint < pixelStartAddress) {
				pixStream.read();
				curPoint ++;
			}
			for(int i = 0; i<pixelSize; i++)
				pixelData[i] = pixStream.read();
			file.close();
			pixStream.close();
		} catch (IOException e) { e.printStackTrace(); };
	}
	
	// Getters
	public int[] getBMPHeader() {
		return bmpHeader;
	}
	
	public int[] getDIBHeader() {
		return dibHeader;
	}
	
	public int[] getHeaderPadding() {
		return headerPadding;
	}
	
	public int[] getPixels() {
		return pixelData;
	}

	public int getPixelSize() {
		return pixelSize;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}
}
