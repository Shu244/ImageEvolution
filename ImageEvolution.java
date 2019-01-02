import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.BoxLayout;

/**
 * Only full circles can be displayed: circles that exceed the image's border.
 * After each generation, the previous generation of circles are erased. 
 * will not exists.
 * @author Shuhao Lai
 */
public class ImageEvolution extends JPanel
{
	private final String directory = "C:\\Users\\junha\\Desktop\\Shuhao Lai\\Eclipse Stuff\\WorkSpace\\Images\\dot.png";
	private final int mutationType = -1; //soft = 1, medium = 2, hard = 3, and random are the choices. Their respective number indicate the number of changes made.  
	private final int popSize = 125;
	private final double endPercent = 98;

//-------------------------User Adjustment Ends Here---------------------------------------------------------------------------------------------------------------------
	
	private int[][][] imageRGB = imageToPixel(directory); // For original image
	private int imageWidth = imageRGB[0].length, imageHeight = imageRGB.length, generationCount = 0;
	private final int maxRadius = imageWidth<imageHeight? imageWidth/3:imageHeight/3, minRadius = imageWidth<imageHeight? imageWidth/10:imageHeight/10; // in pixels.
	private CircleDNA[] pop = new CircleDNA[popSize], mutated = new CircleDNA[popSize]; // For population 
	double imageFitness = 0;
	//double importanceOfSpread = 50, imageFitness = 0; //50%

	public class CircleDNA
	{
		private int radius, red, green, blue, centerX, centerY;
		public CircleDNA(int radius, int red, int green, int blue, int centerX, int centerY)
		{
			this.radius = radius;
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.centerX = centerX;
			this.centerY = centerY;			
		}
		public CircleDNA(CircleDNA copy)
		{
			this.radius = copy.radius;
			this.red = copy.red;
			this.green = copy.green;
			this.blue = copy.blue;
			this.centerX = copy.centerX;
			this.centerY = copy.centerY;
		}
	}
	
	private void firstPopulation()
	{
		int radius = (int) (Math.random() * (maxRadius - minRadius + 1)) + minRadius, centerX, centerY;
		for (int x = 0; x < popSize; x++)
		{
			centerX = (int) (Math.random() * (imageWidth - radius * 2)) + radius;
			centerY = (int) (Math.random() * (imageHeight - radius * 2)) + radius;
			pop[x] = new CircleDNA(radius, (int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256), centerX, centerY);
		}
	}
	private int[][][] imageToPixel(String directory) // [][][0] = red [][][1] = green [][][2] = blue [][][3] = transparency
	{
		try
		{
			BufferedImage image = ImageIO.read(new File(directory));
			int[][][] imageRGB = new int[image.getWidth()][image.getHeight()][4];
			for (int w = 0; w < image.getWidth(); w++)
				for (int h = 0; h < image.getHeight(); h++)
				{
					Color c = new Color(image.getRGB(w, h));
					imageRGB[w][h][0] = c.getRed(); // (0, 0) is the top left corner.
					imageRGB[w][h][1] = c.getGreen();
					imageRGB[w][h][2] = c.getBlue();
					imageRGB[w][h][3] = 127; // 255 means the color is opaque and 0 means the color is completely transparent.
				}
			return imageRGB;
		} 
		catch (IOException ioe)
		{
			System.out.println("Son, picture ain't work. " + ioe);
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * Evaluating the fitness of a child with respect to the distance from the parent. 
	 */
	
	/*
	 * Changing one variation of the CircleDNA and if it improves fitness, it survives; although this is less like 
	 * evolution, it is more efficient. 
	 * this method only checks once to see if a change increases survive, once it checks once, it moves on to another circle.
	 * This method is the best because once a circle is really fit, it will take ages to evolve. 
	 * Crossover keeps animals from diseases but it does not advance a population to become more fit, this is the doing of a mutation.
	 * Since diseases are not an issue in this program, only mutations are made. 
	*/
	private void mutation()
	{
		for(int i = 0; i < popSize; i++)
		{
			int mutationType = this.mutationType;
			mutated[i] = new CircleDNA(pop[i]);	
			if(mutationType == -1)
				mutationType = (int)(Math.random()*3+1);
			ArrayList<Integer> pars = new ArrayList<Integer>();
			pars.add(0); pars.add(1); pars.add(2); pars.add(3); pars.add(4); pars.add(5);
			for(int x = 0; x < mutationType; x++)
			{
				int param = pars.remove((int)(Math.random()*pars.size()));
				do
				{
					if(param == 0)
						mutated[i].radius = (int) (Math.random() * (maxRadius - minRadius + 1)) + minRadius;						
					else if(param == 1)
						mutated[i].red = (int) (Math.random() * 256);
					else if(param == 2)
						mutated[i].green = (int) (Math.random() * 256);
					else if(param == 3)
						mutated[i].blue = (int) (Math.random() * 256);
					else if(param == 4)
						mutated[i].centerX = (int)(Math.random()*(imageWidth - minRadius*2 + 1) + minRadius);
					else if(param == 5)
						mutated[i].centerY = (int)(Math.random()*(imageHeight - minRadius*2 + 1) + minRadius);
				}
				while(mutated[i].centerY - mutated[i].radius < 0 || mutated[i].centerY + mutated[i].radius > imageHeight || mutated[i].centerX - mutated[i].radius < 0 || mutated[i].centerX + mutated[i].radius > imageWidth);
			}		
		}
	}

	//first generation shows as blank.
	public class Painter extends JPanel
	{
		public BufferedImage copyImage(BufferedImage source)
		{
		    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		    Graphics g = b.getGraphics();
		    g.drawImage(source, 0, 0, null);
		    g.dispose();
		    return b;
		}
		
		private BufferedImage buffer, old;
		public Painter()
		{
			buffer = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			setBackground(Color.BLACK); 
			new Timer(0, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (imageFitness < endPercent)
						evolve(buffer.getGraphics());
					else
					{
						((Timer) e.getSource()).stop();
						System.out.println("\n\nDem Noobs Evolved.");
					}
				}
			}).start();
		}
		
		private double fitnessOfMutated()
		{
			int mutatedFitnessSum = 0;
			int[][][] imageRGBMutated;
			BufferedImage image = buffer;
			imageRGBMutated = new int[image.getWidth()][image.getHeight()][3];
			for (int w = 0; w < image.getWidth(); w++)
				for (int h = 0; h < image.getHeight(); h++)
				{
					Color c = new Color(image.getRGB(w, h));
					imageRGB[w][h][0] = c.getRed(); // (0, 0) is the top left corner.
					imageRGB[w][h][1] = c.getGreen();
					imageRGB[w][h][2] = c.getBlue();
				}
			
			for(int x = 0; x < imageRGBMutated.length; x++) 
				for(int y = 0; y < imageRGBMutated[0].length; y++) //Y starting from the top
				{
					mutatedFitnessSum += Math.abs(imageRGB[x][y][0] + imageRGB[x][y][1] + imageRGB[x][y][2] - (imageRGBMutated[x][y][0] + imageRGBMutated[x][y][1] + imageRGBMutated[x][y][2]));
				}
			return (255 * 3 - (double)mutatedFitnessSum/(imageRGBMutated.length*imageRGBMutated[0].length))/(255 * 3) * 100; //Averaging the pixel differences and converting it to a percent. Most unfit pixel is a difference of 255*3.
		}

		int xCorner = imageWidth < 800? 400 - imageWidth/2:0;
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			double mutatedFit = fitnessOfMutated();
			if(imageFitness <= mutatedFit)
			{
				g.drawImage(buffer, xCorner, 0, this); 
				repaint(); //Repaint might be necessary because paintComponent senses no change: paintComponent is unable to understand what's in the bufferedImage, so all bufferedImages are the same. 
				for(int i = 0; i < popSize; i++)
					pop[i] = new CircleDNA(mutated[i]);
				imageFitness = mutatedFit;
				old = copyImage(buffer);
			}
			g.drawImage(old, xCorner, 0, this); 
			repaint();
		}

		private void evolve(Graphics g)
		{
			g.setColor(new Color(240, 240, 240));
			g.fillRect(0, 0, imageWidth, imageHeight);
			for (int i = 0; i < popSize; i++)
			{
				g.setColor(new Color(pop[i].red, pop[i].green, pop[i].blue, 125));
				g.fillOval(pop[i].centerX, pop[i].centerY, pop[i].radius, pop[i].radius);
			}
			generationCount++;
			String fit = imageFitness + "0";
			fitLevel.setText("Fitness level (out of 100): " + fit.substring(0, fit.indexOf(".")+3));
			generation.setText("Generation: " + generationCount);
			mutation();					
		}
	}

	private JLabel fitLevel, generation, mutate, population;
	private JFrame frame = new JFrame("Genetic Images");
	private JPanel panel = new JPanel();
	private void run()
	{
		firstPopulation();
		
		fitLevel = new JLabel("Fitness level (out of 100): NA");
		generation = new JLabel("Generation: " + generationCount);
		mutate = new JLabel("mutation type: " + mutationType);
		population = new JLabel("Population size: " + popSize);

		fitLevel.setFont(new Font("Serif", Font.PLAIN, 50));
		generation.setFont(new Font("Serif", Font.PLAIN, 50));
		mutate.setFont(new Font("Serif", Font.PLAIN, 50));
		population.setFont(new Font("Serif", Font.PLAIN, 50));

		fitLevel.setAlignmentX(Component.CENTER_ALIGNMENT);
		generation.setAlignmentX(Component.CENTER_ALIGNMENT);
		mutate.setAlignmentX(Component.CENTER_ALIGNMENT);
		population.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new Painter());
		panel.add(fitLevel);
		panel.add(generation);
		panel.add(mutate);
		panel.add(population);

		frame.add(panel);
		frame.setSize(800 > imageWidth? 800 : imageWidth, imageHeight + 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public ImageEvolution()
	{
		run();
	}

	/*
	 * Improve fitness method and create an organize method for images. 
	 */
	public static void main(String[] args) throws InterruptedException
	{
		new ImageEvolution();
	}
}








