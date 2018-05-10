//package deprecatedButIStillWantToKeep;
//import java.io.ByteArrayOutputStream;
//import java.io.PipedOutputStream;
//import java.io.PrintStream;
//import java.awt.Canvas;
//import java.awt.Dimension;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JScrollPane;
//import javax.swing.JSplitPane;
//import javax.swing.JTextArea;
//
//import worldBuilder.TextFile;
//import worldBuilder.WorldScript;
//import worldBuilder.WorldTemplate;
//
//
//public class WorldHistoryDisplay implements Runnable{
//
//	public static final int WIDTH = 700;
//	public static final int HEIGHT = 700;
//	private JFrame frame;
//	private Canvas window;
//	private JTextArea status;
//	private JScrollPane statusScroll;
//	private JButton startButton;
//	private JSplitPane leftSplitPane;
//	private JSplitPane splitPane;
//
//	private WorldTemplate template;
//	private ByteArrayOutputStream baos;
//	private PrintStream out;
//	private WorldScript script;
//	private boolean paused;
//
//	public boolean isPaused()
//	{
//		return paused;
//	}
//
//	public void updateText()
//	{
//		status.setText(new String(baos.toByteArray()));
//	}
//	
//	public WorldHistoryDisplay(WorldTemplate t)
//	{
//		template = t;
//		window = new Canvas();
//		baos = new ByteArrayOutputStream();
//		out = new PrintStream(baos);
//		script = new WorldScript(t.getMain(), window, out,0);
//		status = new JTextArea("");
//		statusScroll = new JScrollPane(status);
//		startButton = new JButton("Start");
//		startButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if(isPaused())
//				{ 
//					startButton.setText("Stop");
//					paused = false;
//				}
//				else if(!isPaused())
//				{
//					startButton.setText("Start");
//					paused = true;
//				}
//			}
//		});
//		
//		leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//				statusScroll, startButton);
//		
//		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//				leftSplitPane, window);
//
//	}
//
//	@Override
//	public void run() {
//		frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.getContentPane().add(splitPane);
//		frame.pack();
//		frame.setVisible(true);
//
//		while(true)
//		{
//			try {
//				script.wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			updateText();
//		}
//	}
//
//}
