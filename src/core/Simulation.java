package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

import utils.Vector2f;
import biology.Brain;
import biology.Pellet;
import biology.Protozoa;
import biology.Entity;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	
	public static Random RANDOM;
	
	public Simulation(long seed)
	{
		RANDOM = new Random(seed);
		
		tank = new Tank();

		int creatures = 80;
		int pellets = 160;
		
		for (int i = 0; i < creatures; i++) {
			double radius = (RANDOM.nextInt(5) + 5) / 500.0;
			Protozoa p = new Protozoa(Brain.RANDOM, radius);
			tank.addEntity(p);
		}
		
		for (int i = creatures; i <  creatures + pellets; i++) {
			double radius = (RANDOM.nextInt(3) + 2) / 500.0;
			tank.addEntity(new Pellet(radius));
		}

		for (Entity e : tank.getEntities())
			e.move(new Vector2f(0, 0), tank.getEntities());
		
		simulate = true;

	}
	
	public Simulation()
	{
		this(new Random().nextLong());
	}
	
	@Override
	public void run() 
	{
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		tank.update(timer.getDelay() / 1000.0);
		
		if (simulate)
			timer.restart();
		else
			timer.stop();
	}

	public Tank getTank()
	{
		return tank;
	}
}