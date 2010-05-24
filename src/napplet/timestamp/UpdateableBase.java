package napplet.timestamp;

import static java.lang.System.nanoTime;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateableBase implements Updateable {

	public long timestamp = nanoTime();

	public List<Timestamp> dependsOnList = new ArrayList<Timestamp>();

	public void addDependent(Timestamp t) {
		dependsOnList.add(t);
	}

	public void removeDependent(Timestamp t) {
		if (dependsOnList.contains(t))
			dependsOnList.remove(t);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public abstract void doUpdate();

	public void update() {
		boolean needsUpdating = false;
		for (Timestamp t : dependsOnList) {
			if (t instanceof Updateable)
				((Updateable) t).update();
			if (t.getTimestamp() > this.getTimestamp())
				needsUpdating = true;
		}

		if (needsUpdating) {
			doUpdate();
			timestamp = System.nanoTime();
		}
	}

}
