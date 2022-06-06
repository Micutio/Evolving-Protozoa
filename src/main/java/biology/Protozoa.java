package biology;

import com.google.common.collect.Streams;
import core.Settings;
import core.Simulation;
import core.Tank;
import utils.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Protozoa extends Entity 
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	private float totalConsumption = 0;
	
	private ProtozoaGenome genome;
	private Retina retina;
	private final Brain brain;

	private float shieldFactor = 1.3f;
	private final float attackFactor = 10f;
	private final float consumeFactor = 15f;

	private float splitRadius = Float.MAX_VALUE; // No splitting by default.

	public Protozoa(ProtozoaGenome genome, Tank tank)
	{
		this(genome.brain(), genome.retina(), genome.getRadius(), tank);
		this.genome = genome;
		setGrowthRate(genome.getGrowthRate());
		splitRadius = genome.getSplitRadius();
	}

	public Protozoa(Tank tank) {
		this(new ProtozoaGenome(), tank);
	}

	public Protozoa(Brain brain, Retina retina, float radius, Tank tank)
	{
		super(tank);
		setHealthyColour(new Color(
			100 + Simulation.RANDOM.nextInt(20),
			80 + Simulation.RANDOM.nextInt(50),
			150  + Simulation.RANDOM.nextInt(100)
		));

		this.brain = brain;
		this.retina = retina;
		setPos(new Vector2(0, 0));
		float t = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		setVel(new Vector2(
				(float) (0.1f * Math.cos(t)),
				(float) (0.1f * Math.sin(t))
		));
		this.setRadius(radius);
		setMaxThinkTime(0.2f);
	}

	public void see(Entity e)
	{
		Vector2 dr = getPos().sub(e.getPos());
		Vector2 dir = getDir();
		float rx = dr.dot(dir);
		float ry = dr.dot(dir.perp());
		
		for (Retina.Cell cell : retina) 
		{
			float y = (float) (rx*Math.tan(cell.angle));
			boolean inView = Math.abs(y - ry) <= e.getRadius() && rx < 0;
			
			boolean isBlocked = false;
			if (cell.entity != null) 
				isBlocked = dr.len2() > cell.entity.getPos().squareDistanceTo(getPos());
			
			if (inView && !isBlocked) {
				cell.entity = e;
				cell.colour = e.getColor();
			}
		}
	}
	
	public void eat(Entity e, float delta)
	{
		float consumed = consumeFactor * delta * e.getNutrition();
		totalConsumption += consumed;
		setHealth(getHealth() + consumed);
		e.setHealth(e.getHealth() - consumed);
	}

	public void damage(float damage) {
		setHealth(getHealth() - damage);
	}
	
	public void fight(Protozoa p, float delta)
	{
		float myAttack = (float) (2*getHealth() + 0.3*getRadius() + 2*Simulation.RANDOM.nextDouble());
		float theirAttack = (float) (2*p.getHealth() + 0.3*p.getRadius() + 2*Simulation.RANDOM.nextDouble());

		if (myAttack > p.shieldFactor * theirAttack) {
			damage(delta * attackFactor * (myAttack - p.shieldFactor * theirAttack));
			if (isDead())
				handleDeath();
		}
		else if (theirAttack > shieldFactor * myAttack) {
			p.damage(delta * attackFactor * (theirAttack - shieldFactor * myAttack));
			if (p.isDead())
				p.handleDeath();
		}
	}
	
	public void think(float delta)
	{
		brain.tick(this);
		rotate(delta * 100 * brain.turn(this));
		setSpeed(brain.speed(this));
	}

	private boolean shouldSplit() {
		return getRadius() > splitRadius && getHealth() > Settings.minHealthToSplit;
	}

	private Protozoa createSplitChild(float r) {
		float stuntingFactor = r / getRadius();
		Protozoa child = genome.createChild(tank);
		child.setRadius(stuntingFactor * child.getRadius());
		return child;
	}

	public void interact(Entity other, float delta) {

		if (other == this)
			return;

		if (isDead()) {
			handleDeath();
			return;
		}

		see(other);

		if (shouldSplit()) {
			super.burst(this::createSplitChild);
			return;
		}

		if (isTouching(other)) {

			if (other instanceof Protozoa)
			{
				Protozoa p = (Protozoa) other;

				if (brain.wantToAttack(p))
					fight(p, delta);

//				else if (brain.wantToMateWith(p) && p.brain.wantToMateWith(this)) {
//					// Add some negative consequences of mating?
//					Stream<Entity> children = genome.reproduce(this, p).map(Function.identity());
//					return Streams.concat(newEntities, children);
//				}
			}
			else if (other.isEdible())
				eat(other, delta);

		}
	}

	public void handleInteractions(float delta) {
		resetRetina();
		Iterator<Entity> entities = broadCollisionDetection(Settings.protozoaInteractRange);
		entities.forEachRemaining(e -> interact(e, delta));
	}

	public void resetRetina() {
		for (Retina.Cell cell : retina) {
			cell.colour = Color.WHITE;
			cell.entity = null;
		}
	}

	private void breakIntoPellets() {
		burst(r -> new MeatPellet(r, tank));
	}

	public void handleDeath() {
		if (!hasHandledDeath) {
			super.handleDeath();
			breakIntoPellets();
		}
	}

	@Override
	public String getPrettyName() {
		return "Protozoan";
	}

	@Override
	public HashMap<String, Float> getStats() {
		HashMap<String, Float> stats = super.getStats();
		stats.put("Fitness", getFitness());
		stats.put("Growth Rate", Settings.statsDistanceScalar * getGrowthRate());
		stats.put("Split Radius", Settings.statsDistanceScalar * splitRadius);
		return stats;
	}

	@Override
	public float getGrowthRate() {
		float growthRate = super.getGrowthRate();
		if (getRadius() > splitRadius)
			growthRate *= getHealth() * splitRadius / (5 * getRadius());
		return growthRate;
	}

	public void age(float delta) {
		float deathRate = getRadius() * getSpeed() * delta * 200;
		setHealth(getHealth() * (1 - deathRate));
	}

	@Override
	public void update(float delta)
	{
		super.update(delta);

		age(delta);
		if (isDead())
			handleDeath();

		think(delta);
	}
	
	public void render(Graphics g)
	{
		super.render(g);
		
		float r0 = 1;
		float r1 = 0.8f;
		for (Retina.Cell cell : retina)
		{
			float x = (float) Math.cos(cell.angle + getVel().angle());
			float y = (float) Math.sin(cell.angle + getVel().angle());
			float len = (float) Math.sqrt(x*x + y*y);
			float r2 = r1;// + 0.5 * (1 - r1) * (1 + Math.cos(2*Math.PI*cell.angle));
			g.setColor(cell.colour);
			g.drawLine(
					(int) (getPos().getX() + (x * getRadius() * r0) / len),
					(int) (getPos().getY() + (y * getRadius() * r0) / len),
					(int) (getPos().getX() + (x * getRadius() * r2) / len),
					(int) (getPos().getY() + (y * getRadius() * r2) / len)
			);
		}
	}
	
	@Override
	public float getNutrition() {
		return 20 * getHealth() * getRadius();
	}

	@Override
	public boolean isEdible() {
		return false;
	}

	public Retina getRetina() {
		return retina;
	}

	public void setRetina(Retina retina) {
		this.retina = retina;
	}

	public float getFitness() {
		return totalConsumption;
	}

	public ProtozoaGenome getGenome() {
		return genome;
	}

	public float getShieldFactor() {
		return shieldFactor;
	}

	public void setShieldFactor(float shieldFactor) {
		this.shieldFactor = shieldFactor;
	}

	public Brain getBrain() {
		return brain;
	}
}