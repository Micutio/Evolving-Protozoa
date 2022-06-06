package core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import biology.NNBrain;
import neat.NeuralNetwork;
import neat.Neuron;
import utils.TextObject;
import utils.Vector2;
import utils.Window;
import utils.TextStyle;
import biology.Entity;
import biology.Protozoa;

public class UI 
{
	private Window window;
	private Simulation simulation;
	private TextObject title;
	private ArrayList<TextObject> info;
	private ArrayList<TextObject> debugInfo;
	private int infoTextSize;

	public UI(Window window, Simulation simulation)
	{
		this.window = window;
		this.simulation = simulation;

		title = new TextObject("Evolving Protozoa",
				TextStyle.fontName,
				window.getHeight() / 20, 
				new Vector2(window.getWidth() / 60f, window.getHeight() / 15f));
		title.setColor(Color.WHITE);
		
		info = new ArrayList<>();
		infoTextSize = window.getHeight() / 35;

		TextObject pelletText = new TextObject("Number of pellets: ", infoTextSize);
		pelletText.setColor(Color.WHITE.darker());

		TextObject protozoaText = new TextObject("Number of protozoa: ", infoTextSize);
		protozoaText.setColor(Color.WHITE.darker());

		TextObject trackingFitness = new TextObject("", infoTextSize);
		trackingFitness.setColor(Color.WHITE.darker());


		info.add(protozoaText);
		info.add(pelletText);
		info.add(trackingFitness);

		TextObject fpsText = new TextObject("FPS: ",
				infoTextSize,
				new Vector2(window.getWidth() * 0.9f, window.getHeight() / 20f));
		fpsText.setColor(Color.YELLOW.darker());

		debugInfo = new ArrayList<>();
		debugInfo.add(fpsText);
	}

	public float getYPos(int i) {
		return (1.1f*i + 3) * window.getHeight() / 20f;
	}
	
	public void render(Graphics2D g, Renderer renderer)
	{
		title.render(g);

		int textAwayFromEdge = window.getWidth() / 60;

		int lineNumber;
		for (lineNumber = 0; lineNumber < info.size(); lineNumber++)
			info.get(lineNumber).setPosition(new Vector2(textAwayFromEdge, getYPos(lineNumber)));

		
		info.get(0).setText("Number of pellets: " + simulation.getTank().numberOfPellets());
		info.get(0).render(g);
		info.get(1).setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
		info.get(1).render(g);

		Entity tracked = renderer.getTracked();
		if (tracked != null) {
			if (tracked.isDead() && !tracked.getChildren().isEmpty()) {
				renderer.track(tracked.getChildren().iterator().next());
				tracked = renderer.getTracked();
			}

			lineNumber++;
			TextObject statsTitle = new TextObject(
					tracked.getPrettyName() + " Stats",
					(int) (infoTextSize * 1.1),
					new Vector2(textAwayFromEdge, getYPos(lineNumber))
			);
			statsTitle.setColor(Color.WHITE.darker());
			statsTitle.render(g);

			HashMap<String, Float> stats = tracked.getStats();
			for (Map.Entry<String, Float> entityStat : stats.entrySet()) {
				lineNumber++;
				String text = entityStat.getKey() + ": " + TextStyle.toString(entityStat.getValue(), 2);
				TextObject statText = new TextObject(
						text, infoTextSize,
						new Vector2(textAwayFromEdge, getYPos(lineNumber))
				);
				statText.setColor(Color.WHITE.darker());
				statText.render(g);
			}

			if (tracked instanceof Protozoa && ((Protozoa) tracked).getBrain() instanceof NNBrain) {
				NNBrain brain = (NNBrain) ((Protozoa) tracked).getBrain();
				renderBrainNetwork(brain.network, g);
			}
		}
		else
			info.get(2).setText("");


		if (simulation.inDebugMode()) {
			debugInfo.get(0).setText("FPS: " + (int) renderer.getFPS());
			debugInfo.get(0).render(g);
		}
	}

	private void renderBrainNetwork(NeuralNetwork nn, Graphics2D g) {
		int networkDepth = nn.getDepth();
		int boxWidth = 2 * window.getWidth() / 10;
		int boxHeight = 2 * window.getHeight() / 3;

		int boxXStart = window.getWidth() - (int) (boxWidth * 1.05);
		int boxYStart = (window.getHeight() - boxHeight) / 2;

		if (simulation.inDebugMode()) {
			g.setColor(Color.YELLOW.darker());
			g.drawRect(boxXStart, boxYStart, boxWidth, boxHeight);
			for (int yHeight = boxYStart; yHeight < boxYStart + boxHeight; yHeight += yHeight / networkDepth)
				g.drawLine(boxXStart, yHeight, boxXStart + boxWidth, yHeight);
		}

		HashMap<Neuron, Vector2> neuronPositions = new HashMap<>();

	}
}