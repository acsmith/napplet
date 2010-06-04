package napplet.timestamp;

public class UpdateManager {

	public static boolean recursiveUpdate(Updateable u) {

		long uTimestamp = u.getTimestamp();
		boolean needsUpdate = false;

		for (Timestamp t : u.dependsOnList()) {

			if (t instanceof Updateable)
				recursiveUpdate((Updateable) t);

			long tTimestamp = t.getTimestamp();
			needsUpdate = needsUpdate || (tTimestamp > uTimestamp);
		}

		if (needsUpdate)
			u.update();

		return needsUpdate;
	}

}
