import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import javax.script.*;

public class BasicHeightMap {

  // inshortform
  private static double makecontintentmappoint(int x, int y, OpenSimplexNoise rangeNoise, OpenSimplexNoise shapeNoise,
      OpenSimplexNoise baseNoise) {
    double scale = 2 << 19;
    double imountrangesize = 2;
    double ifisrangesize = 3;
    double icontinentsize = 1;
    float mheight = (float) (rangeNoise.eval(((float) x) / (scale / imountrangesize),
        ((float) y) / (scale / imountrangesize)));
    mheight = 1f - Math.abs(mheight);
    float fheight = (float) (rangeNoise.eval(((float) (x)) / (scale / ifisrangesize), // same scale, different area
        ((float) y) / (scale / ifisrangesize)));
    fheight = -(float) Math.pow(1 - Math.abs(fheight), 3);
    float cheight = (float) (shapeNoise.eval(((float) x) / (scale / icontinentsize),
        ((float) y) / (scale / icontinentsize)));
    cheight = (cheight + 1) / 2;
    double[][] LayerWeightsAndiSizes = new double[][] { { 2 << 2, 2 << 3, 2 << 4, 2 << 5, 2 << 6, 2 << 7, 2 << 8, },
        { 17, 15, 13, 10, 7, 5, 3, } };

    float layerweighttotal = 0;
    float rheight = 0;
    for (int i = 0; i < LayerWeightsAndiSizes[0].length; i++) {
      double layersize = scale / LayerWeightsAndiSizes[0][i];
      double layerweight = LayerWeightsAndiSizes[1][i];
      double lheight = (baseNoise.eval(x / layersize, (y / layersize)));
      lheight *= layerweight;
      rheight += lheight;
      layerweighttotal += LayerWeightsAndiSizes[1][i];
    }
    rheight = rheight / layerweighttotal;
    rheight = (rheight + 1) / 2;
    float height = mheight * 0.2f + fheight * 0.1f + rheight * 0.45f + cheight * 0.45f;
    // height = fheight;
    return Math.pow(height, 4);
  }

  public static double size = 1;

  private static short[][] makeContinentDigestArray(int xsize, int ysize, int seed) {
    OpenSimplexNoise Noise1 = new OpenSimplexNoise(seed + 0);
    OpenSimplexNoise Noise2 = new OpenSimplexNoise(seed + 1);
    OpenSimplexNoise Noise3 = new OpenSimplexNoise(seed + 2);
    int digestxsize = 700;
    int digestysize = 700;
    System.out.println("In the beginning, the world was drawn forth from the ether...");
    short[][] map = new short[digestxsize][digestysize];
    for (int x = 0; x < digestxsize; x += 1) {
      for (int y = 0; y < digestysize; y += 1) {
        // long startTime = System.nanoTime();
        double rawheight = makecontintentmappoint((xsize / digestxsize) * x, (ysize / digestysize) * y, Noise1, Noise2,
            Noise3);
        short height = ShortMap.DoubleToShort(rawheight);
        map[x][y] = height;

        // System.out.println(System.nanoTime() - startTime);
      }
      // System.out.println(x);
    }
    return map;
  }

  private static void draw(Color[][] colors) {
    int xsizem = colors.length;
    int ysizem = colors[0].length;
    BufferedImage img = new BufferedImage(xsizem, ysizem, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < xsizem; x += 1) {
      for (int y = 0; y < ysizem; y += 1) {
        Color color = colors[x][y];
        if (color == null) {
          color = Color.black;
        }
        img.setRGB(x % img.getWidth(), y, color.getRGB());
        if (y + 1 == ysizem && x % img.getWidth() == img.getWidth() - 1) {
          Main.game.drawImage(img, x - img.getWidth(), 0);
        }
      }
    }

  }

  public static void draw(int scale, short[][] map) {
    Color[][] cmap = new Color[map.length * scale][map[0].length * scale];
    Color c;
    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[0].length; y++) {
        int val = (int) (255 * ShortMap.ShortToDouble(map[x][y]));
        c = new Color(val, val, val);
        for (int x1 = 0; x1 < scale; x1++) {
          for (int y1 = 0; y1 < scale; y1++) {
            cmap[scale * x + x1][scale * y + y1] = c;
          }
        }
      }
    }
    draw(cmap);
  }

  public static int seed = 42;

  public static void kill() {
    int[] erro = new int[2];
    erro[3] = 0;
  }

  private static void create(int xsize, int ysize, double sealevel) {
    short[][] map = makeContinentDigestArray(xsize, ysize, seed);

    int xsizem = map.length;
    int ysizem = map[0].length;

    draw(1, map);

    // Temperature.illuminance(2048, 2048);
    // Temperature.getTemperature(map,seed,sealevel);
    // kill();
    // we make an array that tells to which point water will flow
    byte[][][] flowstowhere = new byte[xsizem][ysizem][2];
    // Then we fill holes and tell the water where to go
    final int slevel = ShortMap.DoubleToShort(sealevel);
    int plevel = (slevel - 4000);
    byte[][] exploremap = new byte[xsizem][ysizem];// explored, tells what has been touched, and what not
    byte[][] replacementmap = new byte[xsizem][ysizem];//
    // wang & liu algorithm. Starts from any spots that are Under 9000 XDDDDD

    for (int x = 0; x < xsizem; x++) {
      for (int y = 0; y < ysizem; y++) {
        if (map[x][y] < plevel || x == 0 || y == 0 || x == xsizem - 1 || y == ysizem - 1) {
          exploremap[x][y] = 1;
          replacementmap[x][y] = 1;
        }
      }
    }
    boolean keepgoing = true;
    ArrayList<Point> pointlist = new ArrayList<Point>(4);
    pointlist.add(new Point(-1, 0));
    pointlist.add(new Point(1, 0));
    pointlist.add(new Point(0, -1));
    pointlist.add(new Point(0, 1));
    Color[][] cmap = new Color[xsizem][ysizem];
    while (keepgoing) {
      keepgoing = false;
      // raise the water level
      plevel += 5;
      for (int x = 0; x < xsizem; x++) {
        for (int y = 0; y < ysizem; y++) {
          if (exploremap[x][y] == 1)// if this is an discovered, but unexplored tile
          {
            keepgoing = true;// keep going
            byte freeedges = 0;// unexplored edges of this tile.
            for (int i = 0; i < pointlist.size(); i++) {
              int x1 = pointlist.get(i).x, y1 = pointlist.get(i).y;
              int rx = x + x1, ry = y + y1;
              // if it is within range and x or y is zero and the selected target is
              // unexplored
              if (rx >= 0 && ry >= 0 && rx < xsizem && ry < ysizem && exploremap[rx][ry] == 0) {
                freeedges += 1;
                if (map[rx][ry] < plevel) {
                  replacementmap[rx][ry] = 1;
                  float v = (float) (ShortMap.ShortToDouble((short) plevel) * 8);
                  Color c = Color.getHSBColor(v, 1, 0.5f);
                  Main.g2d.setPaint(c);
                  Main.g2d.fillRect(rx, ry, 1, 1);
                  // The water flows from rx to x
                  flowstowhere[rx][ry][0] = (byte) -x1;
                  flowstowhere[rx][ry][1] = (byte) -y1;
                }
              }
            }
            if (freeedges < 1) {
              replacementmap[x][y] = 2;
              // map[x][y] = (short)((plevel*4 + map[x][y])/5);
              map[x][y] = (short) (plevel);
            }
          }
        }
      }
      for (int x = 0; x < map.length; x++) {
        for (int y = 0; y < map[0].length; y++) {
          exploremap[x][y] = replacementmap[x][y];
        }
      }
    }
    ShortMap b = new ShortMap(map);
    map = b.getMap();
    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[0].length; y++) {
        short h = map[x][y];
        short val = (short) (255 * ShortMap.ShortToDouble(h));
        if (h < slevel) {
          val = 0;
        }
        cmap[x][y] = new Color(val, val, val);

      }
    }
    draw(cmap);
    // kill();
    new ShortMap(map).Export("./WorldSave/Elevation.png");
    Screen.wind();

    // next lets find out where the rivers should go.
    int[][] rivScore = new int[xsizem][ysizem];
    for (int x = 0; x < xsizem; x++) {
      System.out.println(x);
      for (int y = 0; y < ysizem; y++) {
        int x1 = x;
        int y1 = y;
        int timeout = 0;
        while (timeout < 1000) {
          timeout++;
          try {
            rivScore[x1][y1] += 1;
            // System.out.println(" "+rivScore[x1][y1]);
            if (flowstowhere[x1][y1][0] == 0 && flowstowhere[x1][y1][1] == 0) {
              rivScore[x1][y1] = -1;
              break;
            }
            int nx1 = x1 + flowstowhere[x1][y1][0];
            int ny1 = y1 + flowstowhere[x1][y1][1];
            x1 = nx1;
            y1 = ny1;
          } catch (ArrayIndexOutOfBoundsException e) {
            break;
          }
        }
      }
    }
    HashMap<Point, River> rivermap = new HashMap<Point, River>();
    for (int x = 0; x < xsizem; x++) {
      for (int y = 0; y < ysizem; y++) {
        if (// rivScore[x][y] == 30
        (x % 30 == 0 && y % 30 == 0) && !rivermap.containsKey(new Point(x + 0, y + 0))
            && !rivermap.containsKey(new Point(x + 0, y + 1)) && !rivermap.containsKey(new Point(x + 0, y - 1))
            && !rivermap.containsKey(new Point(x + 1, y + 0)) && !rivermap.containsKey(new Point(x + 1, y + 1))
            && !rivermap.containsKey(new Point(x + 1, y - 1)) && !rivermap.containsKey(new Point(x - 1, y + 0))
            && !rivermap.containsKey(new Point(x - 1, y + 1)) && !rivermap.containsKey(new Point(x - 1, y - 1))) {
          River river = new River();
          int timeout = 0;
          int x1 = x;
          int y1 = y;
          Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
          Main.g2d.setPaint(c);
          runRiver: while (timeout < 10000) {
            timeout++;
            int mxscr = -2;
            int flx = 0;
            int fly = 0;
            Collections.shuffle(pointlist);
            checkNeighbors: for (int i = 0; i < pointlist.size(); i++) {
              int rx = pointlist.get(i).x + x1, ry = pointlist.get(i).y + y1;
              if (rx >= 0 && ry >= 0 && rx < xsizem && ry < ysizem) {

                if (rivermap.containsKey(new Point(rx, ry))) {
                  River riverCollidedWith = rivermap.get(new Point(rx, ry));
                  if (river != riverCollidedWith) {
                    /*
                     * // if(river.getPower(river.length()-1) < //
                     * riverCollidedWith.getPower(river.length()-1)) { // riverCollidedWith = river;
                     * }
                     * 
                     * 
                     * int colloc = riverCollidedWith.getLocationPoint(rx, ry);
                     * while(riverCollidedWith.length()-1 > colloc) {
                     * riverCollidedWith.removePoint(riverCollidedWith.length()-1); }
                     * while(rivermap.containsValue(riverCollidedWith)) { //remember to only remove
                     * the lower values rivermap.values().remove(riverCollidedWith); }
                     */
                    break runRiver;
                  }
                }

                int scr = rivScore[rx][ry];
                if (scr == -1 || map[rx][ry] < slevel) {
                  river.appendNewPoint(rx, ry, mxscr, River.RIVER_END_REACH_OCEAN);
                  break runRiver;
                }
                if (scr > mxscr) {
                  mxscr = scr;
                  flx = rx;
                  fly = ry;
                }
              }
            }

            Main.g2d.fillRect(x1, y1, 1, 1);

            river.appendNewPoint(x1, y1, rivScore[x1][y1], River.RIVER_NORMAL);
            rivermap.put(new Point(x1, y1), river);
            x1 = flx;
            y1 = fly;
          }
          if (river == null || river.length() < 3) {
            rivermap.values().remove(river);
            rivermap.values().remove(river);
            rivermap.values().remove(river);
          }
        }
      }
    }
    System.out.println("...and traced.");
    for (int x = 0; x < xsizem; x++) {
      for (int y = 0; y < ysizem; y++) {
        short h = map[x][y];
        short hval = // (short)(1+ Math.min(254,rivScore[x][y]/20));

            (short) (255 * ShortMap.ShortToDouble(h));
        if (h < slevel) {
          hval = 0;
        }
        cmap[x][y] = // new Color(0,0,255,hval);
            new Color(hval, hval, hval);

      }
    }
    draw(cmap);

    ArrayList<River> rivlist = new ArrayList<River>();
    rivlist.addAll(rivermap.values());
    Set<River> hs = new HashSet<River>();
    hs.addAll(rivlist);
    rivlist.clear();
    rivlist.addAll(hs);
    // rivlist.addAll(); rivermap.values()
    for (int i = 0; i < rivlist.size(); i++) {
      Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
      Main.g2d.setPaint(c);
      River r = rivlist.get(i);
      for (int q = 0; q < r.length(); q++) {
        Main.g2d.fillRect(r.getX(q), r.getY(q), 1, 1);
      }
    }

    kill();


    Color[][] colormap = new Color[xsizem][ysizem];

    Temperature.getTemperature(map, seed, sealevel);
    ShortMap tmap = new ShortMap("./WorldSave/Temperature.png");
    for (int x = 0; x < xsizem; x++) {
      for (int y = 0; y < ysizem; y++) {
        if (map[x][y] > ShortMap.DoubleToShort(sealevel)) {
          double tmp  =  Temperature.TemperatureShortToDouble(tmap.get(x, y));
          if (tmp > 80) {
            colormap[x][y] = Color.RED;
          } else if (tmp > 70) {
            colormap[x][y] = Color.YELLOW;
          } else if (tmp > 50) {
            colormap[x][y] = Color.GREEN.brighter().brighter();
          } else if (tmp > 30) {
            colormap[x][y] = Color.GREEN;
          } else {
            colormap[x][y] = Color.BLUE;
          }
        } else {
          colormap[x][y] = new Color(0, 0, 0);
        }
      }
    }
    draw(colormap); //

    kill();

    // *
    ShortMap rmap = new ShortMap("./WorldSave/RainMap.png");
    for (int x = 0; x < xsizem; x++) {
      for (int y = 0; y < ysizem; y++) {
        if (map[x][y] > ShortMap.DoubleToShort(sealevel)) {
          if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.6) {
            colormap[x][y] = Color.green.darker().darker();
          } else if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.5) {
            colormap[x][y] = Color.green;
          } else if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.4) {
            colormap[x][y] = Color.green.brighter().brighter();
          } else if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.3) {
            colormap[x][y] = new Color(255, 255, 0);
          } else if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.2) {
            colormap[x][y] = new Color(255, 255, 0).brighter();
          } else if (ShortMap.ShortToDouble(rmap.get(x, y)) > 0.1) {
            colormap[x][y] = new Color(200, 200, 200);
          } else {
            colormap[x][y] = Color.gray;
          }
        } else {
          colormap[x][y] = new Color(0, 0, 0);
        }
      }
    }
    draw(colormap);
    // */

  }

  public static double[][][] getBasicHeightMap(int xSize, int ySize, double seaLevel) {
    create(xSize, ySize, seaLevel);
    return null;
  }

}