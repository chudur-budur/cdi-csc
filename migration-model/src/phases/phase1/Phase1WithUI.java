package phases.phase1;

import phases.BasePhaseWithUI;
import sim.display.Console;

public class Phase1WithUI extends BasePhaseWithUI
{
	
	
	public Phase1WithUI() {

		super(new Phase1(System.currentTimeMillis(), new String[]{"-file", "data/parameters/phase.param"}));
		model = (Phase1)state;
	}
	

	
	public static String getName() {
		return "Phase 1";
	}

	public static void main(String[] args)
	{
		Phase1WithUI phase1 = new Phase1WithUI();

		Console c = new Console(phase1);
		c.setVisible(true);
		// Map map = new Map();
	}
}


