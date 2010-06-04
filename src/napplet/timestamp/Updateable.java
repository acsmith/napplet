package napplet.timestamp;

public interface Updateable extends Timestamp {
	
	public Iterable<Timestamp> dependsOnList();
	
	public abstract void update();
}
