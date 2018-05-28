package worldUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import fastnoise.FastNoise;
import tester.Main;


public class WorldUtils {
	
	public static HeightMap fillBasins(HeightMap h, double seaLevel)
	{
		
		Graphics2D g2d = (Graphics2D) Main.c.getGraphics();
		final int xSize = h.getXSize();
		final int ySize = h.getYSize();
		double[][] map = h.getMap();
		
		//Then we fill holes and tell the water where to go
		double plevel = seaLevel;
		byte[][] exploremap = new byte[xSize][ySize];//explored, tells what has been touched, and what not
		byte[][] replacementmap = new byte[xSize][ySize];//

		//wang & liu algorithm. Starts from any spots that are Under 9000 XDDDDD
		for(int x = 0; x < xSize; x++)
		{
			for(int y = 0; y < ySize; y++)
			{
				//the points to start exploring from
				if(map[x][y] < plevel || x==0 ||y==0 ||x==xSize-1||y==ySize-1)
 				{
					exploremap[x][y] = 1;
					replacementmap[x][y] = 1;
				}
			}
		}
		
		boolean keepgoing = true;
		ArrayList<Point> pointlist = new ArrayList<Point>(4);
		pointlist.add(new Point(-1,0));pointlist.add(new Point(1,0));
		pointlist.add(new Point(0,-1));pointlist.add(new Point(0,1));

		byte freeedges = 0;//unexplored edges of this tile.
		while(keepgoing)
		{
			keepgoing = false;
			//raise the water level
			plevel+=0.001;
			System.out.println(plevel);
			for(int x = 0; x < xSize; x++)
			{
				for(int y = 0; y < ySize; y++)
				{
					if(exploremap[x][y] == 1)//if this is an discovered, but unexplored tile
					{
						keepgoing = true;//keep going
						for(int i = 0; i < pointlist.size(); i++)
						{
							int x1 = pointlist.get(i).x, y1 = pointlist.get(i).y;
							int rx=x+x1, ry=y+y1; 
							//if it is within range and x or y is zero and the selected target is unexplored
							if(rx>=0&&ry>=0&&rx<xSize&&ry<ySize&&exploremap[rx][ry] == 0)
							{
								freeedges+=1;
								if(map[rx][ry] < plevel)
								{
									g2d.setPaint(Color.getHSBColor((float)plevel, 0.7f, 0.7f));
									g2d.fillRect(rx, ry, 1, 1);
									replacementmap[rx][ry] = 1;
									//The water flows from rx to x
								}
							}
						}
						if(freeedges < 3)
						{
							replacementmap[x][y] = 2;
							map[x][y] = plevel;
						}
					}
				}
			}
			for(int x = 0; x < map.length; x++)
			{
				for(int y = 0; y < map[0].length; y++)
				{
					exploremap[x][y] = replacementmap[x][y];
				}
			}
		}
		HeightMap b = new HeightMap(map);
		return b;
	}
}
