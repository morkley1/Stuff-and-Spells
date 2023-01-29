package StuffAndSpells.common.casting;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			PatternRegistry.mapPattern(
				HexPattern.fromAngles("awd", HexDir.SOUTH_WEST), 
				modLoc("example"),
				OpExample.INSTANCE
			);
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}