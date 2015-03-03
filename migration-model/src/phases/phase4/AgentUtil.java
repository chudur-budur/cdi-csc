package phases.phase4;

import org.spiderland.Psh.intStack;


public class AgentUtil {

	public static double getMovementWill(Phase4 model, int agentType)
	{
		switch(agentType)
		{
		case 0:
			return model.parameters.urbanMovementWill;
		case 1:
			return model.parameters.ruralMovementWill;
		default:
			System.err.println("this should never happen");
			return 0.0;
		}
	}
	
	public static double getGrowthRate(Phase4 model, int agentType)
	{
		switch(agentType)
		{
		case 0:
			return model.parameters.urbanGrowthRate;
		case 1:
			return model.parameters.ruralGrowthRate;
		default:
			System.err.println("this should never happen");
			return 0.0;
		}
	}
	
	public static double getSocialWeight(Phase4 model, int agentType)
	{
		switch(agentType)
		{
		case 0:
			return model.parameters.urbanSocialWeight;
		case 1:
			return model.parameters.ruralSocialWeight;
		default:
			System.err.println("this should never happen");
			return 0.0;
		}
	}
	
	
	public static double getAdjacentSocialDiscount(Phase4 model, int agentType)
	{
		switch(agentType)
		{
		case 0:
			return model.parameters.urbanAdjacentSocialDiscount;
		case 1:
			return model.parameters.ruralAdjacentSocialDiscount;
		default:
			System.err.println("this should never happen");
			return 0.0;
		}
	}

	public static double getSatisfaction(Phase4 model, int cellIndex, int agentType) {
		switch(agentType)
		{
		case 0:
			return model.worldAgent.urbanSatisfaction[cellIndex];
		case 1:
			return model.worldAgent.ruralSatisfaction[cellIndex];
		default:
			System.err.println("this should never happen");
			return 0.0;
		}
	}
}

