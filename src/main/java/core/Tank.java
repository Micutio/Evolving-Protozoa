package core;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utils.Vector2;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;

public class Tank implements Iterable<Entity>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private final double radius = 2;
	private HashMap<Class<? extends Entity>, Integer> entityCounts;
	private final ChunkManager chunkManager;

	public Tank() 
	{
		double chunkSize = 2 * radius / 10;
		chunkManager = new ChunkManager(-radius, radius, -radius, radius, chunkSize);
		entityCounts = new HashMap<>();
	}
	
	public void addEntity(Entity e) {
		double rad 	= radius - 2*e.getRadius();
		double t 	= 2 * Math.PI * Simulation.RANDOM.nextDouble();
		double r 	= Simulation.RANDOM.nextDouble();
		e.setPos(new Vector2(
					rad * (1 - r*r) * Math.cos(t),
					rad * (1 - r*r) * Math.sin(t)
				));
		chunkManager.add(e);

		if (!entityCounts.containsKey(e.getClass()))
			entityCounts.put(e.getClass(), 0);
		else
			entityCounts.put(e.getClass(), 1 + entityCounts.get(e.getClass()));
	}

	public Stream<Entity> updateEntity(Entity e, double delta) {

		Stream<Entity> newEntities = e.update(delta, chunkManager.getNearbyEntities(e));

		if (e.getPos().len() - e.getRadius() > radius)
			e.setPos(e.getPos().mul(-0.98));

		if (e.isDead())
			entityCounts.put(e.getClass(), -1 + entityCounts.get(e.getClass()));

		return newEntities;
	}

	public void update(double delta) 
	{
		Collection<Entity> newEntities = chunkManager.getAllEntities()
				.flatMap(e -> updateEntity(e, delta))
				.collect(Collectors.toList());

		newEntities.forEach(chunkManager::add);
		chunkManager.update();
	}
	
	public void render(Graphics g)
	{
		chunkManager.forEachEntity(e -> e.render(g));
	}

	public Collection<Entity> getEntities() {
		return chunkManager.getAllEntities().collect(Collectors.toList());
	}
	
	public int numberOfProtozoa() {
		return entityCounts.get(Protozoa.class);
	}
	
	public int numberOfPellets() {
		return entityCounts.get(Pellet.class);
	}

	@Override
	public Iterator<Entity> iterator() {
		return chunkManager.getAllEntities().iterator();
	}
}
