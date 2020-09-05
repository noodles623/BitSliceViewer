import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitSlice extends Thread {
	
	private ByteArrayInputStream bitPlane;
	private ByteArrayInputStream[] bitPlanes;
	private String addr;
	private int plane;
	
	public BitSlice(String addr, int plane, boolean cgc) {
		bitPlanes = new ByteArrayInputStream[25];
		this.addr = addr;
		this.plane = plane;
		BitMap bm = new BitMap(addr);
		int[][] pixels = sliceBitPlanes(bm, cgc);
		for(int i = 0; i < 25; i++)
			bitPlanes[i] = imageInputStream(bm.getBMPHeader(), bm.getDIBHeader(),
											bm.getHeaderPadding(), pixels[i]);
	}
	
	public ByteArrayInputStream[] getBitPlanes() {
		return bitPlanes;
	}
	
	public void run(boolean cgc) {
		BitMap bm = new BitMap(addr);
		int[] pixels;
		if(plane == -1) 
			pixels = simpleCopy(bm, cgc);
		else
			pixels = sliceBitPlane(bm, plane, cgc);
		bitPlane = imageInputStream(bm.getBMPHeader(),bm.getDIBHeader(),
									bm.getHeaderPadding(), pixels);		
	}
	
	private static int getPlane(int plane) {
		assert (plane >=0 && plane < 24);
		return 128>>(plane%8);
	}
	
	public ByteArrayInputStream getBitPlane() {
		return bitPlane;
	}
	
	// Converts  bit-plane into binary image
	public int[] sliceBitPlane(BitMap bm, int plane, boolean cgc) {
				
		int[] pixels = bm.getPixels();
		int[] bitPlane = new int[pixels.length];
		
		// https://en.wikipedia.org/wiki/BMP_file_format#Pixel_storage
		int rowSize = (int) (Math.ceil((24 * bm.getImageWidth()) / 32.0) * 4);
		int padding = rowSize - bm.getImageWidth() * 3;
		for(int row = 0; row < bm.getImageHeight(); row++)
		{
			int offset = row * rowSize;
			// Process 1 pixel at a time
			for(int col = 0; col < bm.getImageWidth()*3; col+=3) {
				int pixelVal = pixels[offset+col+(plane / 8)];
				if(cgc==true) pixelVal = toCGC(pixelVal);

				int val = 0xff;
				if((pixelVal & getPlane(plane)) == 0) 
					val = 0x00;
				for(int k = 0; k < 3; k++)
					bitPlane[offset+col+k] = val; 
			}
			// Add padding
			for(int i = 0; i < padding; i++)
				bitPlane[offset + (bm.getImageWidth()*3) + i] = 0x00;
		}
		return bitPlane;
	}
	
	// Converts all bit-planes at once
	public int[][] sliceBitPlanes(BitMap bm, boolean cgc) {
				
		int[] pixels = bm.getPixels();
		int[][] bitPlanes = new int[25][pixels.length];
		
		// https://en.wikipedia.org/wiki/BMP_file_format#Pixel_storage
		int rowSize = (int) (Math.ceil((24 * bm.getImageWidth()) / 32.0) * 4);
		int padding = rowSize - bm.getImageWidth() * 3;
		for(int row = 0; row < bm.getImageHeight(); row++)
		{
			int offset = row * rowSize;
			// Process 1 pixel at a time
			for(int col = 0; col < bm.getImageWidth()*3; col+=3) {
				// Original copy
				for(int k = 0; k < 3; k++) {
					int pixelVal = pixels[offset+col+k];
					if(cgc==true) pixelVal = toCGC(pixelVal);
					bitPlanes[24][offset+col+k] = pixelVal;
				}
				for(int plane = 0; plane < 24; plane++) {
					int pixelVal = pixels[offset+col+(plane / 8)];
					if(cgc==true) pixelVal = toCGC(pixelVal);

					int val = 0xff;
					if((pixelVal & getPlane(plane)) == 0) 
						val = 0x00;
					for(int k = 0; k < 3; k++)
						bitPlanes[plane][offset+col+k] = val; 
				}
			}
			// Add padding
			for(int plane = 0; plane < 25; plane++) {
				for(int i = 0; i < padding; i++)
					bitPlanes[plane][offset + (bm.getImageWidth()*3) + i] = 0x00;
			}
		}
		return bitPlanes;
	}
	
	public int[] simpleCopy(BitMap bm, boolean cgc) {
		
		int[] pixels = bm.getPixels();
		int[] bitPlane = new int[pixels.length];
		
		int rowSize = (int) (Math.ceil((24 * bm.getImageWidth()) / 32.0) * 4);
		int padding = rowSize - bm.getImageWidth() * 3;
//		int padding2 = Math.floorMod(bm.getImageWidth() * -3, 4);
		
		int totalBytes = 0;
		for(int row = 0; row < bm.getImageHeight(); row++)
		{
			int offset = row * rowSize;
			// Process 1 pixel at a time
			for(int col = 0; col < bm.getImageWidth()*3; col+=3) {
				for(int k = 0; k < 3; k++) {
					int pixelVal = pixels[offset+col+k];
					if(cgc==true) pixelVal = toCGC(pixelVal);
					bitPlane[offset+col+k] = pixelVal;
					totalBytes++;
				}
			}
			// Add padding
			for(int i = 0; i < padding; i++) {
				bitPlane[offset + (bm.getImageWidth()*3) + i] = 0x00;
				totalBytes++;
			}
		}
		return bitPlane;
	}
	
	public void writeBitPlane(BitMap bm, int[] bitPlane, String destination) {
		try {
			FileOutputStream file = new FileOutputStream(destination);
			BufferedOutputStream bos = new BufferedOutputStream(file);
			// Write Headers
			int[] bmpHeader = bm.getBMPHeader();
			int[] dibHeader = bm.getDIBHeader();
			int[] headerPadding = bm.getHeaderPadding();
 
			for(int i = 0; i < bmpHeader.length; i++)
				bos.write(bmpHeader[i]);
			for(int i = 0; i < dibHeader.length; i++)
				bos.write(dibHeader[i]);
			for(int i = 0; i < headerPadding.length; i++)
				bos.write(headerPadding[i]);
			// Write Pixel Data
			for(int i = 0; i < bitPlane.length; i++)
				bos.write(bitPlane[i]);
			bos.flush();
			file.close();
			bos.close();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public ByteArrayInputStream imageInputStream(int[] bmpHeader, int[] dibHeader, int[] headerPadding, int[] bitPlane) {
		byte[] bytes = new byte[bmpHeader.length+dibHeader.length+headerPadding.length+bitPlane.length];
		
		for(int i = 0; i < bmpHeader.length; i++)
			bytes[i] = (byte) bmpHeader[i];
		for(int i = 0; i < dibHeader.length; i++)
			bytes[i+bmpHeader.length] = (byte) dibHeader[i];
		for(int i = 0; i < headerPadding.length; i++)
			bytes[i+bmpHeader.length+dibHeader.length] = (byte) headerPadding[i];
		for(int i = 0; i < bitPlane.length; i++)
			bytes[i+bmpHeader.length+dibHeader.length+headerPadding.length] = (byte) bitPlane[i];
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return is;
	}

	private int toCGC(int pcb) {
		return ((pcb<<1)^pcb)>>1;
	}
}
